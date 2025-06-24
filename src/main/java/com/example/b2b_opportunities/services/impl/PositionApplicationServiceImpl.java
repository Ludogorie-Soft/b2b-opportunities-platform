package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.dto.requestDtos.PositionApplicationRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.CompanyApplicationResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.PositionApplicationResponseDto;
import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.Position;
import com.example.b2b_opportunities.entity.PositionApplication;
import com.example.b2b_opportunities.entity.Project;
import com.example.b2b_opportunities.entity.Talent;
import com.example.b2b_opportunities.entity.User;
import com.example.b2b_opportunities.exception.ServerErrorException;
import com.example.b2b_opportunities.exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.exception.common.InvalidRequestException;
import com.example.b2b_opportunities.exception.common.NotFoundException;
import com.example.b2b_opportunities.exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.mapper.PositionApplicationMapper;
import com.example.b2b_opportunities.repository.PositionApplicationRepository;
import com.example.b2b_opportunities.repository.TalentRepository;
import com.example.b2b_opportunities.services.interfaces.MailService;
import com.example.b2b_opportunities.services.interfaces.PositionApplicationService;
import com.example.b2b_opportunities.services.interfaces.PositionService;
import com.example.b2b_opportunities.services.interfaces.UserService;
import com.example.b2b_opportunities.enums.ApplicationStatus;
import com.example.b2b_opportunities.enums.ProjectStatus;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionApplicationServiceImpl implements PositionApplicationService {
    private final UserService userService;
    private final CompanyServiceImpl companyService;
    private final ProjectServiceImpl projectService;
    private final PositionService positionService;
    private final PositionApplicationRepository positionApplicationRepository;
    private final ImageServiceImpl imageService;
    private final MailService mailService;
    private final MinioClient minioClient;
    private final TalentRepository talentRepository;

    @Value("${storage.bucketName}")
    private String bucketName;

    @Value("${storage.url}")
    private String storageUrl;

    private static ApplicationStatus calculateOverallStatus(List<PositionApplication> applications) {
        return applications.stream()
                .map(PositionApplication::getApplicationStatus)
                .filter(status -> status != ApplicationStatus.AWAITING_CV_OR_TALENT)
                .min(Comparator.comparingInt(PositionApplicationServiceImpl::getPriority))
                .orElse(ApplicationStatus.IN_PROGRESS);
    }


    private static int getPriority(ApplicationStatus status) {
        return switch (status) {
            case ACCEPTED -> 1;
            case IN_PROGRESS -> 2;
            case DENIED -> 3;
            default -> Integer.MAX_VALUE;
        };
    }

    @PostConstruct
    private void init() {
        // Change storageUrl if it's set to http://minio:9000 - to make it work while testing in docker.
        if (storageUrl.toLowerCase().contains("minio:9000")) {
            storageUrl = "http://localhost:9000";
        }
    }

    @Override
    public PositionApplicationResponseDto applyForPosition(Authentication authentication, PositionApplicationRequestDto requestDto) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        Position position = positionService.getPositionOrThrow(requestDto.getPositionId());
        Project project = position.getProject();
        checkPositionEligibility(project, position, userCompany);

        PositionApplication application = PositionApplication.builder()
                .position(position)
                .applicationStatus(ApplicationStatus.AWAITING_CV_OR_TALENT)
                .applicationDateTime(LocalDateTime.now())
                .lastUpdateDateTime(LocalDateTime.now())
                .rate(requestDto.getRate())
                .availableFrom(requestDto.getAvailableFrom())
                .talentCompany(userCompany)
                .build();

        if (requestDto.getTalentId() != null) {
            Talent talent = companyService.getTalentOrThrow(requestDto.getTalentId());
            validateApplication(userCompany, position, talent);
            talent.setLastAppliedAt(LocalDateTime.now());
            talentRepository.save(talent);
            application.setTalent(talent);
            application.setApplicationStatus(ApplicationStatus.IN_PROGRESS);
        }

        PositionApplication pa = positionApplicationRepository.save(application);
        return PositionApplicationMapper.toPositionApplicationResponseDto(pa);
    }

    @Override
    public PositionApplicationResponseDto uploadCV(MultipartFile file, Long applicationId) {
        log.info("Attempting to upload CV for application ID: {}", applicationId);
        try {
            // Use the input stream directly from the MultipartFile
            InputStream inputStream = file.getInputStream();

            // Upload the CV to MinIO
            PutObjectArgs pArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object("CV/" + applicationId)
                    .stream(inputStream, file.getSize(), -1) // Pass the file size and unknown part size (-1)
                    .contentType(file.getContentType()) // Ensure the correct content type is set
                    .build();

            minioClient.putObject(pArgs);

            inputStream.close();
            log.info("Uploaded CV for application ID: {}", applicationId);

            PositionApplication pa = getPositionApplicationOrThrow(applicationId);
            pa.setApplicationStatus(ApplicationStatus.IN_PROGRESS);
            positionApplicationRepository.save(pa);

            return generatePAResponse(pa);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ServerErrorException("Error occurred while uploading file: " + e.getMessage());
        }
    }

    @Override
    public boolean doesCVExist(Long positionApplicationId) {
        try {
            String objectName = "CV/" + positionApplicationId;

            // Check if the object exists by fetching its metadata
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            } else {
                throw new ServerErrorException("Error occurred while checking CV file for ID: " + positionApplicationId + " existence: " + e.getMessage());
            }
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ServerErrorException("Error occurred while checking object existence: " + e.getMessage());
        }
    }

    @Override
    public String returnUrlIfCVExists(Long positionApplicationId) {
        if (doesCVExist(positionApplicationId)) {
            return storageUrl + "/" + bucketName + "/CV/" + positionApplicationId;
        }
        return null;
    }

    @Override
    public List<PositionApplicationResponseDto> getApplicationsForMyPositions(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        List<Project> projects = userCompany.getProjects();
        List<Position> positions = projects.stream().flatMap(project -> project.getPositions().stream()).toList();
        if (positions.isEmpty()) {
            return new ArrayList<>();
        }
        List<ApplicationStatus> excludedStatuses = List.of(ApplicationStatus.CANCELLED, ApplicationStatus.AWAITING_CV_OR_TALENT);
        List<PositionApplication> positionApplications = positionApplicationRepository.findAllApplicationsForMyPositions(userCompany.getId(), excludedStatuses);
        List<PositionApplicationResponseDto> responseDtoList = new ArrayList<>();
        for (PositionApplication pa : positionApplications) {
            PositionApplicationResponseDto positionApplicationResponseDto = generatePAResponse(pa);
            positionApplicationResponseDto.setCompanyName(pa.getTalentCompany().getName());
            positionApplicationResponseDto.setCompanyImage(imageService.returnUrlIfPictureExists(pa.getTalentCompany().getId(), "image"));
            responseDtoList.add(positionApplicationResponseDto);
        }
        return responseDtoList;
    }

    @Override
    public List<PositionApplicationResponseDto> getMyApplications(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        List<PositionApplication> myApplications = positionApplicationRepository.findAllMyApplications(userCompany.getId(), ApplicationStatus.CANCELLED);
        if (myApplications.isEmpty()) {
            return new ArrayList<>();
        }
        List<PositionApplicationResponseDto> responseDtoList = new ArrayList<>();
        for (PositionApplication pa : myApplications) {
            responseDtoList.add(generatePAResponse(pa));
        }
        return responseDtoList;
    }

    @Override
    public PositionApplicationResponseDto acceptApplication(Authentication authentication, Long applicationId) {
        return updatePositionApplicationStatus(authentication,
                applicationId,
                ApplicationStatus.ACCEPTED,
                ApplicationStatus.DENIED,
                "This application has been denied and cannot be accepted"
        );
    }

    @Override
    public PositionApplicationResponseDto rejectApplication(Authentication authentication, Long applicationId) {
        return updatePositionApplicationStatus(authentication,
                applicationId,
                ApplicationStatus.DENIED,
                ApplicationStatus.ACCEPTED,
                "This application has been accepted and cannot be denied"
        );
    }

    @Override
    public PositionApplicationResponseDto getApplicationById(Authentication authentication, Long applicationId) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        PositionApplication pa = getPositionApplicationOrThrow(applicationId);
        validateAccess(pa, userCompany);

        PositionApplicationResponseDto responseDto = generatePAResponse(pa);
        responseDto.setCompanyName(pa.getTalentCompany().getName());
        responseDto.setCompanyImage(imageService.returnUrlIfPictureExists(pa.getTalentCompany().getId(), "image"));
        return responseDto;
    }

    @Override
    public PositionApplicationResponseDto updateApplication(Authentication authentication, MultipartFile file, Long applicationId, Long talentId) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        PositionApplication pa = getPositionApplicationOrThrow(applicationId);
        if (!Objects.equals(pa.getTalentCompany().getId(), userCompany.getId())) {
            throw new PermissionDeniedException("This application was not made by your company");
        }
        if (talentId != null) {
            Talent talent = companyService.getTalentOrThrow(talentId);
            companyService.validateTalentBelongsToCompany(userCompany, talent);
            pa.setTalent(talent);
            pa.setApplicationStatus(ApplicationStatus.IN_PROGRESS);
            positionApplicationRepository.save(pa);
        }
        if (file != null && !file.isEmpty()) {
            uploadCV(file, applicationId);
            pa.setApplicationStatus(ApplicationStatus.IN_PROGRESS);
            positionApplicationRepository.save(pa);
        }
        return generatePAResponse(pa);
    }

    @Override
    public List<PositionApplication> getApplicationsSinceLastWorkday(){
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        int days = (dayOfWeek == DayOfWeek.MONDAY) ? 3 : 1;

        LocalDate startDate = today.minusDays(days);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.atStartOfDay();

        return positionApplicationRepository.findAllApplicationsBetween(startDateTime, endDateTime);
    }

    @Override
    public List<CompanyApplicationResponseDto> getMyApplicationsOverall(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        List<PositionApplication> myApplications = positionApplicationRepository
                .findAllMyApplications(userCompany.getId(), ApplicationStatus.CANCELLED);
        return myApplications.stream()
                .collect(Collectors.groupingBy(pa -> pa.getPosition().getId()))
                .entrySet().stream()
                .map(entry -> {
                    Long positionId = entry.getKey();
                    List<PositionApplication> apps = entry.getValue();

                    Set<Long> applicationIds = apps.stream()
                            .map(PositionApplication::getId)
                            .collect(Collectors.toSet());

                    ApplicationStatus overallStatus = calculateOverallStatus(apps);

                    Long companyId = getApplicationsCompanyId(apps);
                    LocalDateTime lastUpdateDateTime = getLatestDateTime(apps);
                    return new CompanyApplicationResponseDto(positionId, companyId, applicationIds, overallStatus, lastUpdateDateTime);
                })
                .toList();
    }

    @Override
    public void cancelApplication(Authentication authentication, Long applicationId) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        PositionApplication positionApplication = getPositionApplicationOrThrow(applicationId);
        if (!Objects.equals(positionApplication.getTalentCompany().getId(), userCompany.getId())) {
            throw new PermissionDeniedException("This position application does not belong to your company");
        }
        positionApplication.setApplicationStatus(ApplicationStatus.CANCELLED);
        positionApplicationRepository.save(positionApplication);
    }

    private LocalDateTime getLatestDateTime(List<PositionApplication> positionApplications) {
        return positionApplications.stream()
                .map(PositionApplication::getLastUpdateDateTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private Long getApplicationsCompanyId(List<PositionApplication> positionApplications){
        return positionApplications.stream()
                .findFirst()
                .map(pa -> pa.getPosition().getProject().getCompany().getId())
                .orElse(null);
    }

    private void checkPositionEligibility(Project project, Position position, Company userCompany) {
        projectService.validateProjectIsAvailableToCompany(project, userCompany);
        if (project.getCompany().getId().equals(userCompany.getId())) {
            throw new InvalidRequestException("You can't apply to a position that belongs to your company!", "positionId");
        }
        if (project.getProjectStatus().equals(ProjectStatus.INACTIVE)) {
            throw new InvalidRequestException("This position belongs to a project that is inactive", "positionId");
        }
        if (!position.getStatus().getId().equals(1L)) {
            throw new InvalidRequestException("The position is not opened", "positionId");
        }
    }

    private PositionApplicationResponseDto generatePAResponse(PositionApplication pa) {
        PositionApplicationResponseDto responseDto = PositionApplicationMapper.toPositionApplicationResponseDto(pa);
        responseDto.setCvUrl(returnUrlIfCVExists(pa.getId()));
        return responseDto;
    }

    private void validateAccess(PositionApplication pa, Company userCompany) {
        boolean isMatchingCompany = pa.getTalentCompany().equals(userCompany);
        boolean isMatchingProjectCompany = pa.getPosition().getProject().getCompany().getId().equals(userCompany.getId());
        if (!isMatchingCompany && !isMatchingProjectCompany) {
            throw new PermissionDeniedException("This application was not created to/by your company/project");
        }
    }

    private PositionApplicationResponseDto updatePositionApplicationStatus(
            Authentication authentication,
            Long applicationId,
            ApplicationStatus targetStatus,
            ApplicationStatus invalidStatus,
            String invalidStatusMessage
    ) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        PositionApplication pa = getPositionApplicationOrThrow(applicationId);

        validateApplicationBelongsToCompany(pa, userCompany);

        if(pa.getApplicationStatus() == ApplicationStatus.CANCELLED) {
            throw new InvalidRequestException("This application has been cancelled");
        }
        if (pa.getApplicationStatus() == targetStatus) {
            return PositionApplicationMapper.toPositionApplicationResponseDto(pa);
        }
        if (pa.getApplicationStatus() == invalidStatus) {
            throw new InvalidRequestException(invalidStatusMessage);
        }
        pa.setApplicationStatus(targetStatus);
        pa.setLastUpdateDateTime(LocalDateTime.now());

        if (targetStatus == ApplicationStatus.ACCEPTED) {
            mailService.sendEmailWhenApplicationIsApproved(pa);
        }

        PositionApplicationResponseDto positionApplicationResponseDto = generatePAResponse(positionApplicationRepository.save(pa));
        positionApplicationResponseDto.setCompanyName(pa.getTalentCompany().getName());
        positionApplicationResponseDto.setCompanyImage(imageService.returnUrlIfPictureExists(pa.getTalentCompany().getId(), "image"));

        return positionApplicationResponseDto;
    }

    private void validateApplicationBelongsToCompany(PositionApplication pa, Company company) {
        if (!Objects.equals(company.getId(), pa.getPosition().getProject().getCompany().getId())) {
            throw new PermissionDeniedException("This application does not belong to your company");
        }
    }

    private PositionApplication getPositionApplicationOrThrow(Long id) {
        return positionApplicationRepository.findById(id).orElseThrow(() -> new NotFoundException("Position application with ID :" + id + " not found"));
    }

    private void validateApplication(Company userCompany, Position position, Talent talent) {
        positionApplicationRepository.findFirstByPositionIdAndTalentIdAndApplicationStatusIn(
                        position.getId(),
                        talent.getId(),
                        List.of(ApplicationStatus.IN_PROGRESS, ApplicationStatus.ACCEPTED))
                .ifPresent(application -> {
                    if (application.getApplicationStatus().equals(ApplicationStatus.IN_PROGRESS)) {
                        throw new AlreadyExistsException("You've already applied for this position", "positionId");
                    } else if (application.getApplicationStatus().equals(ApplicationStatus.ACCEPTED)) {
                        throw new AlreadyExistsException("You're already accepted for this position", "positionId");
                    }
                });

        companyService.validateTalentBelongsToCompany(userCompany, talent);
    }
}