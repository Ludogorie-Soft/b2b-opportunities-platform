package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.PositionApplicationRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionApplicationResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Entity.PositionStatus;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Mapper.PositionApplicationMapper;
import com.example.b2b_opportunities.Repository.PositionApplicationRepository;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import com.example.b2b_opportunities.Static.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PositionApplicationService {
    private final UserService userService;
    private final CompanyService companyService;
    private final ProjectService projectService;
    private final PositionService positionService;
    private final PositionApplicationRepository positionApplicationRepository;

    public PositionApplicationResponseDto applyForPosition(Authentication authentication, PositionApplicationRequestDto requestDto) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        Position position = positionService.getPositionOrThrow(requestDto.getPositionId());
        Project project = position.getProject();
        Talent talent = companyService.getTalentOrThrow(requestDto.getTalentId());

        validateApplication(userCompany, project, position, talent);

        PositionApplication application = PositionApplication.builder()
                .talent(talent)
                .position(position)
                .applicationStatus(ApplicationStatus.IN_PROGRESS)
                .applicationDateTime(LocalDateTime.now())
                .rate(requestDto.getRate())
                .availableFrom(requestDto.getAvailableFrom())
                .build();

        return PositionApplicationMapper.toPositionApplicationResponseDto(positionApplicationRepository.save(application));
    }

    public List<PositionApplicationResponseDto> getApplicationsForMyPositions(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        List<Project> projects = userCompany.getProjects();
        List<Position> positions = projects.stream().flatMap(project -> project.getPositions().stream()).toList();
        if (positions.isEmpty()) {
            return new ArrayList<>();
        }
        List<PositionApplication> positionApplications = positionApplicationRepository.findAllApplicationsForCompany(userCompany.getId());
        return PositionApplicationMapper.toPositionApplicationDtoList(positionApplications);
    }

    public List<PositionApplicationResponseDto> getMyApplications(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        List<PositionApplication> myApplications = positionApplicationRepository.findAllMyApplications(userCompany.getId());
        if (myApplications.isEmpty()) {
            return new ArrayList<>();
        }
        return PositionApplicationMapper.toPositionApplicationDtoList(myApplications);
    }

    public PositionApplicationResponseDto acceptApplication(Authentication authentication, Long applicationId) {
        return updatePositionApplicationStatus(authentication,
                applicationId,
                ApplicationStatus.ACCEPTED,
                ApplicationStatus.DENIED,
                "This application has been denied and cannot be accepted"
        );
    }

    public PositionApplicationResponseDto rejectApplication(Authentication authentication, Long applicationId) {
        return updatePositionApplicationStatus(authentication,
                applicationId,
                ApplicationStatus.DENIED,
                ApplicationStatus.ACCEPTED,
                "This application has been accepted and cannot be denied"
        );
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
        if (pa.getApplicationStatus() == targetStatus) {
            return PositionApplicationMapper.toPositionApplicationResponseDto(pa);
        }
        if (pa.getApplicationStatus() == invalidStatus) {
            throw new InvalidRequestException(invalidStatusMessage);
        }
        pa.setApplicationStatus(targetStatus);
        return PositionApplicationMapper.toPositionApplicationResponseDto(
                positionApplicationRepository.save(pa));
    }

    private void validateApplicationBelongsToCompany(PositionApplication pa, Company company) {
        if (!Objects.equals(company.getId(), pa.getPosition().getProject().getCompany().getId())) {
            throw new PermissionDeniedException("This application does not belong to your company");
        }
    }

    private PositionApplication getPositionApplicationOrThrow(Long id) {
        return positionApplicationRepository.findById(id).orElseThrow(() -> new NotFoundException("Position application with ID :" + id + " not found"));
    }

    private void validateApplication(Company userCompany, Project project, Position position, Talent talent) {
        positionApplicationRepository.findFirstByPositionIdAndTalentIdAndApplicationStatusIn(
                        position.getId(),
                        talent.getCompany().getId(),
                        List.of(ApplicationStatus.IN_PROGRESS, ApplicationStatus.ACCEPTED))
                .ifPresent(application -> {
                    if (application.getApplicationStatus().equals(ApplicationStatus.IN_PROGRESS)) {
                        throw new AlreadyExistsException("You've already applied for this position", "positionId");
                    } else if (application.getApplicationStatus().equals(ApplicationStatus.ACCEPTED)) {
                        throw new AlreadyExistsException("You're already accepted for this position", "positionId");
                    }
                });

        companyService.validateTalentBelongsToCompany(userCompany, talent);
        projectService.validateProjectIsAvailableToCompany(project, userCompany);

        if (project.getCompany().getId().equals(userCompany.getId())) {
            throw new InvalidRequestException("You can't apply to a position that belongs to your company!", "positionId");
        }
        if (project.getProjectStatus().equals(ProjectStatus.INACTIVE)) {
            throw new InvalidRequestException("This position belongs to a project that is inactive");
        }
        if (!position.getStatus().getId().equals(1L)) {
            throw new InvalidRequestException("The position is not opened");
        }

    }
}
