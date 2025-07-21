package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.dto.requestDtos.PositionEditRequestDto;
import com.example.b2b_opportunities.dto.requestDtos.PositionRequestDto;
import com.example.b2b_opportunities.dto.requestDtos.RateRequestDto;
import com.example.b2b_opportunities.dto.requestDtos.RequiredSkillsDto;
import com.example.b2b_opportunities.dto.responseDtos.PositionResponseDto;
import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.Location;
import com.example.b2b_opportunities.entity.Pattern;
import com.example.b2b_opportunities.entity.Position;
import com.example.b2b_opportunities.entity.Project;
import com.example.b2b_opportunities.entity.Rate;
import com.example.b2b_opportunities.entity.RequiredSkill;
import com.example.b2b_opportunities.entity.Skill;
import com.example.b2b_opportunities.entity.User;
import com.example.b2b_opportunities.entity.WorkMode;
import com.example.b2b_opportunities.enums.ApplicationStatus;
import com.example.b2b_opportunities.enums.ProjectStatus;
import com.example.b2b_opportunities.exception.common.InvalidRequestException;
import com.example.b2b_opportunities.exception.common.NotFoundException;
import com.example.b2b_opportunities.mapper.PositionMapper;
import com.example.b2b_opportunities.mapper.RateMapper;
import com.example.b2b_opportunities.repository.LocationRepository;
import com.example.b2b_opportunities.repository.PatternRepository;
import com.example.b2b_opportunities.repository.PositionApplicationRepository;
import com.example.b2b_opportunities.repository.PositionRepository;
import com.example.b2b_opportunities.repository.PositionStatusRepository;
import com.example.b2b_opportunities.repository.ProjectRepository;
import com.example.b2b_opportunities.repository.RateRepository;
import com.example.b2b_opportunities.repository.RequiredSkillRepository;
import com.example.b2b_opportunities.repository.SeniorityRepository;
import com.example.b2b_opportunities.repository.SkillRepository;
import com.example.b2b_opportunities.repository.WorkModeRepository;
import com.example.b2b_opportunities.services.interfaces.PositionService;
import com.example.b2b_opportunities.services.interfaces.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionServiceImpl implements PositionService {
    private final PositionApplicationRepository positionApplicationRepository;
    private final ProjectRepository projectRepository;
    private final SeniorityRepository seniorityRepository;
    private final SkillRepository skillRepository;
    private final PositionRepository positionRepository;
    private final RateRepository rateRepository;
    private final WorkModeRepository workModeRepository;
    private final PositionStatusRepository positionStatusRepository;
    private final UserService userService;
    private final LocationRepository locationRepository;
    private final PatternRepository patternRepository;
    private final RequiredSkillRepository requiredSkillRepository;
    private final CurrencyServiceImpl currencyService;
    private final ProjectServiceImpl projectService;
    private final CompanyServiceImpl companyService;

    @Override
    public PositionResponseDto createPosition(PositionRequestDto dto, Authentication authentication) {
        validateUserAndCompany(authentication);
        validateProjectAndUserAreRelated(dto.getProjectId(), authentication);

        Position position = PositionMapper.toPosition(dto);

        setProjectOrThrow(position, dto.getProjectId());
        setPatternOrThrow(position, dto.getPatternId());

        checkForNonAssignableSkills(dto.getRequiredSkills());
        setPositionFields(position, dto);
        updateProjectDateUpdated(position);

        extendProjectDurationAndActivateIfNeeded(position.getProject());

        position.setStatus(positionStatusRepository.findById(1L).orElseThrow());
        position.setViews(0L);

        if (dto.getLocation() != null) {
            position.setLocation(getLocationIfExists(dto.getLocation()));
        }
        log.info("Successfully created position ID: {} for project ID: {}", position.getId(), dto.getProjectId());
        return PositionMapper.toResponseDto(positionRepository.save(position));
    }

    @Override
    public PositionResponseDto editPosition(Long id, PositionEditRequestDto dto, Authentication authentication) {
        validateUserAndCompany(authentication);
        Position position = getPositionOrThrow(id);

        validateProjectAndUserAreRelated(position.getProject().getId(), authentication);

        position.setMinYearsExperience(dto.getMinYearsExperience());
        position.setHoursPerWeek(dto.getHoursPerWeek());
        position.setResponsibilities(dto.getResponsibilities());
        position.setHiringProcess(dto.getHiringProcess());
        position.setDescription(dto.getDescription());
        position.setPattern(companyService.getPatternOrThrow(dto.getPatternId()));
        setPositionStatusOrThrow(position, dto.getStatusId());

        //if status keeps being 'Opened'
        if (dto.getStatusId() == 1) {
            extendProjectDurationAndActivateIfNeeded(position.getProject());
        }

        checkForNonAssignableSkills(dto.getRequiredSkills());
        deleteAllRequiredSkillsForPositionIfAny(position);
        setPositionFields(position, dto);

        updateProjectDateUpdated(position);
        log.info("Successfully edited position ID: {} for project ID: {}", position.getId(), dto.getProjectId());
        return PositionMapper.toResponseDto(positionRepository.save(position));
    }

    @Override
    public void deletePosition(Long id, Authentication authentication) {
        Position position = getPositionOrThrow(id);
        validateProjectAndUserAreRelated(position.getProject().getId(), authentication);
        positionRepository.delete(position);
        if (!positionRepository.existsOpenedPositionByProjectId(position.getProject().getId())) {
            Project project = position.getProject();
            project.setProjectStatus(ProjectStatus.INACTIVE);
            projectRepository.save(project);
        }
        log.info("Successfully deleted position ID: {} for project ID: {}", position.getId(), position.getProject().getId());
    }

    @Override
    public PositionResponseDto getPosition(Authentication authentication, Long id) {
        Position position = getPositionOrThrow(id);
        Project project = position.getProject();
        Company company = companyService.getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        projectService.validateProjectIsAvailableToCompany(project, company);
        incrementPositionViews(position);
        return generatePositionResponseDto(position);
    }

    @Override
    public Page<PositionResponseDto> getPositions(Authentication authentication,
                                                  int offset,
                                                  int pageSize,
                                                  String sort,
                                                  boolean ascending,
                                                  Integer rate,
                                                  Set<Long> workModes,
                                                  Set<Long> skills,
                                                  Boolean isPartnerOnly) {
        Company userCompany = companyService.getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));

        if (pageSize <= 0) {
            pageSize = 10;
        }

        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sort));

        Page<Position> resultPage = positionRepository.findPositionsByFilters(
                isPartnerOnly,
                isPartnerOnly != null && isPartnerOnly ? userCompany.getId() : null,
                ProjectStatus.ACTIVE,
                rate,
                workModes,
                skills,
                userCompany.getId(),
                pageable
        );

        List<PositionResponseDto> dtos = resultPage.getContent().stream()
                .map(PositionMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, resultPage.getTotalElements());
    }


    @Override
    public void editPositionStatus(Long positionId, Long statusId, String customCloseReason, Authentication authentication) {
        validateUserAndCompany(authentication);
        Position position = getPositionOrThrow(positionId);
        validateProjectAndUserAreRelated(position.getProject().getId(), authentication);

        setPositionStatusOrThrow(position, statusId);

        if (statusId.equals(5L)) {
            if (customCloseReason == null || customCloseReason.isEmpty()) {
                throw new InvalidRequestException("Custom close reason must be entered", "customCloseReason");
            }
            position.setCustomCloseReason(customCloseReason);
        } else {
            position.setCustomCloseReason(null);
        }

        if (statusId == 1L) {
            extendProjectDurationAndActivateIfNeeded(position.getProject());
        } else {
            deactivateProjectIfNoActivePositions(position.getProject());
        }

        updateProjectDateUpdated(position);
        log.info("Successfully changed position ID: {} status to: {}", position.getId(), position.getStatus().getName());

        positionRepository.save(position);
    }

    @Override
    public Position getPositionOrThrow(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Position with ID: " + id + " not found"));
    }

    private PositionResponseDto generatePositionResponseDto(Position position) {
        PositionResponseDto responseDto = PositionMapper.toResponseDto(position);
        Long applicationAmount = (long) positionApplicationRepository.findByPositionIdExcludingAwaitingCvOrTalent(position.getId()).size();
        Long acceptedApplicationsAmount = (long) positionApplicationRepository.findByPositionIdAndApplicationStatus(position.getId(), ApplicationStatus.ACCEPTED).size();
        responseDto.setApplications(applicationAmount);
        responseDto.setApprovedApplications(acceptedApplicationsAmount);
        responseDto.setViews(position.getViews());
        return responseDto;
    }

    private void incrementPositionViews(Position position) {
        position.setViews(position.getViews() + 1);
        positionRepository.save(position);
    }

    private void validateUserAndCompany(Authentication authentication) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        companyService.getUserCompanyOrThrow(currentUser);
    }

    private Location getLocationIfExists(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new NotFoundException("Location with ID: " + id + " not found"));
    }

    private void updateProjectDateUpdated(Position position) {
        Project project = position.getProject();
        project.setDateUpdated(LocalDateTime.now());
        projectRepository.save(project);
    }

    private void setPositionFields(Position position, PositionRequestDto dto) {
        setSeniorityOrThrow(position, dto.getSeniority());
        setWorkModeOrThrow(position, dto.getWorkMode());
        if (dto.getRate() != null) {
            setRate(position, dto.getRate());
        } else {
            RateRequestDto requestDto = new RateRequestDto();
            setRate(position, requestDto);
        }
        setRequiredSkillsForPosition(position, dto.getRequiredSkills());
        setOptionalSkills(position, dto.getOptionalSkills());
    }

    private void validateProjectAndUserAreRelated(Long projectId, Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = companyService.getUserCompanyOrThrow(user);
        Set<Long> projectIds = company.getProjects().stream()
                .map(Project::getId)
                .collect(Collectors.toSet());
        if (!projectIds.contains(projectId)) {
            throw new NotFoundException("Project ID: " + projectId + " is not associated with company ID: " + company.getId() + " and user: " + user.getUsername());
        }
    }

    private void setProjectOrThrow(Position position, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Project with ID: " + projectId + " was not found"));
        position.setProject(project);
    }

    private void setPatternOrThrow(Position position, Long patternId) {
        Pattern pattern = patternRepository.findById(patternId).orElseThrow(() -> new NotFoundException("Pattern with ID: " + patternId + " was not found"));
        position.setPattern(pattern);
    }

    private void setPositionStatusOrThrow(Position position, Long positionStatusId) {
        position.setStatus(positionStatusRepository.findById(positionStatusId)
                .orElseThrow(() -> new NotFoundException("Status with ID: " + positionStatusId + " was not found")));
    }

    private void setSeniorityOrThrow(Position position, Long seniorityId) {
        position.setSeniority(seniorityRepository.findById(seniorityId)
                .orElseThrow(() -> new NotFoundException("Seniority with ID: " + seniorityId + " was not found")));
    }

    private void setWorkModeOrThrow(Position position, @NotNull List<Long> workModeIds) {
        Set<WorkMode> workModes = new HashSet<>(getWorkModesOrThrow(workModeIds));
        position.setWorkModes(workModes);
    }

    private List<WorkMode> getWorkModesOrThrow(List<Long> workModeIdsList) {
        if (workModeIdsList != null) {
            List<WorkMode> workModes = workModeRepository.findAllById(workModeIdsList);

            if (workModes.size() != workModeIdsList.size()) {
                List<Long> foundWorkModeIds = workModes.stream().map(WorkMode::getId).toList();
                List<Long> missingIds = workModeIdsList.stream().filter(id -> !foundWorkModeIds.contains(id)).toList();
                throw new NotFoundException("WorkMode with ID(s): " + missingIds + " not found");
            }
            return workModes;
        }
        return new ArrayList<>();
    }

    private void setRate(Position position, RateRequestDto rateRequestDto) {
        if (rateRequestDto.getMax() != null && rateRequestDto.getMin() != null &&
                rateRequestDto.getMin() > rateRequestDto.getMax()) {
            throw new InvalidRequestException("Min rate cannot exceed max rate", "minRate");
        }
        Rate rate = RateMapper.toRate(rateRequestDto);
        if (rate.getMin() != null) {
            rate.setCurrency(currencyService.getById(2L));
        } else {
            rate.setCurrency(currencyService.getById(4L));
        }
        position.setRate(rateRepository.save(rate));
    }


    private void setRequiredSkillsForPosition(Position position, List<RequiredSkillsDto> dto) {
        if (dto == null || dto.isEmpty()) {
            throw new NotFoundException("RequiredSkillsDto is null or empty");
        }
        position.setRequiredSkills(getRequiredSkillsList(dto, position));
    }

    private void checkForNonAssignableSkills(List<RequiredSkillsDto> dto) {
        List<Long> skillIdList = dto.stream().map(RequiredSkillsDto::getSkillId).toList();
        for (Long skillId : skillIdList) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new NotFoundException("Skill with ID: " + skillId + " not found"));
            if (!skill.getAssignable()) {
                throw new InvalidRequestException("Skill with ID: " + skillId + " is not assignable");
            }
        }
    }

    private void setOptionalSkills(Position position, List<Long> skills) {
        if (skills == null || skills.isEmpty()) {
            return;
        }
        position.setOptionalSkills(skillRepository.findAllById(skills));
    }

    private List<RequiredSkill> getRequiredSkillsList(List<RequiredSkillsDto> dto, Position position) {
        List<RequiredSkill> requiredSkillList = new ArrayList<>();
        for (RequiredSkillsDto requiredSkill : dto) {
            RequiredSkill requiredSkillResult = getRequiredSkill(requiredSkill, position);
            requiredSkillList.add(requiredSkillResult);
        }
        return requiredSkillList;
    }

    private RequiredSkill getRequiredSkill(RequiredSkillsDto requiredSkill, Position position) {
        Skill skill = getSkillOrThrow(requiredSkill.getSkillId());

        RequiredSkill requiredSkillResult = new RequiredSkill();
        requiredSkillResult.setPosition(position);
        requiredSkillResult.setSkill(skill);
        requiredSkillResult.setMonths(requiredSkill.getMonths());
        return requiredSkillResult;
    }

    private Skill getSkillOrThrow(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Skill with ID: " + id + " was not found"));
    }

    private void deleteAllRequiredSkillsForPositionIfAny(Position position) {
        List<RequiredSkill> requiredSkillList = requiredSkillRepository.findByPosition(position);
        if (!requiredSkillList.isEmpty()) {
            requiredSkillRepository.deleteAllById(requiredSkillList.stream().map(RequiredSkill::getId).toList());
        }
    }

    private void deactivateProjectIfNoActivePositions(Project project) {
        List<Position> positions = project.getPositions();
        boolean hasActivePosition = false;
        for (Position position : positions) {
            if (position.getStatus().getId() == 1L) {
                hasActivePosition = true;
                break;
            }
        }
        if (!hasActivePosition) {
            project.setProjectStatus(ProjectStatus.INACTIVE);
            projectRepository.save(project);
        }
    }

    private void extendProjectDurationAndActivateIfNeeded(Project project) {
        project.setExpiryDate(LocalDateTime.now().plusWeeks(3));
        if (project.getProjectStatus().equals(ProjectStatus.INACTIVE)) {
            project.setProjectStatus(ProjectStatus.ACTIVE);
        }
        projectRepository.save(project);
    }
}
