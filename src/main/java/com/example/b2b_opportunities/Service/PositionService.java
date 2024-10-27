package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Request.RateRequestDto;
import com.example.b2b_opportunities.Dto.Request.RequiredSkillsDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Experience;
import com.example.b2b_opportunities.Entity.Location;
import com.example.b2b_opportunities.Entity.Pattern;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Entity.WorkMode;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.ExperienceMapper;
import com.example.b2b_opportunities.Mapper.PositionMapper;
import com.example.b2b_opportunities.Mapper.RateMapper;
import com.example.b2b_opportunities.Repository.ExperienceRepository;
import com.example.b2b_opportunities.Repository.LocationRepository;
import com.example.b2b_opportunities.Repository.PatternRepository;
import com.example.b2b_opportunities.Repository.PositionRepository;
import com.example.b2b_opportunities.Repository.PositionStatusRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Repository.RateRepository;
import com.example.b2b_opportunities.Repository.RequiredSkillRepository;
import com.example.b2b_opportunities.Repository.SeniorityRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import com.example.b2b_opportunities.Repository.WorkModeRepository;
import com.example.b2b_opportunities.Static.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
public class PositionService {
    private final ProjectRepository projectRepository;
    private final SeniorityRepository seniorityRepository;
    private final SkillRepository skillRepository;
    private final PositionRepository positionRepository;
    private final RateRepository rateRepository;
    private final ExperienceRepository experienceRepository;
    private final WorkModeRepository workModeRepository;
    private final PositionStatusRepository positionStatusRepository;
    private final UserService userService;
    private final LocationRepository locationRepository;
    private final PatternRepository patternRepository;
    private final RequiredSkillRepository requiredSkillRepository;

    public PositionResponseDto createPosition(PositionRequestDto dto, Authentication authentication) {
        userService.validateUserAndCompany(authentication);
        validateProjectAndUserAreRelated(dto.getProjectId(), authentication);

        Position position = PositionMapper.toPosition(dto);

        setProjectOrThrow(position, dto.getProjectId());
        setPatternOrThrow(position, dto.getPatternId());
        setPositionFields(position, dto);
        updateProjectDateUpdated(position);
        activateProjectIfInactive(position.getProject());
        position.setStatus(positionStatusRepository.findById(1L).orElseThrow());

        if (dto.getLocation() != null) {
            position.setLocation(getLocationIfExists(dto.getLocation()));
        }
      
        return PositionMapper.toResponseDto(positionRepository.save(position));
    }

    public PositionResponseDto editPosition(Long id, PositionRequestDto dto, Authentication authentication) {
        userService.validateUserAndCompany(authentication);
        Position position = getPositionOrThrow(id);

        validateProjectAndUserAreRelated(position.getProject().getId(), authentication);

        position.setMinYearsExperience(dto.getMinYearsExperience());
        position.setHoursPerWeek(dto.getHoursPerWeek());
        position.setResponsibilities(dto.getResponsibilities());
        position.setHiringProcess(dto.getHiringProcess());
        position.setDescription(dto.getDescription());

        deleteAllRequiredSkillsForPositionIfAny(position);
        setPositionFields(position, dto);

        updateProjectDateUpdated(position);

        return PositionMapper.toResponseDto(positionRepository.save(position));
    }

    public void deletePosition(Long id, Authentication authentication) {
        Position position = getPositionOrThrow(id);
        validateProjectAndUserAreRelated(position.getProject().getId(), authentication);
        positionRepository.delete(position);
    }

    public PositionResponseDto getPosition(Long id) {
        return PositionMapper.toResponseDto(getPositionOrThrow(id));
    }

    public List<PositionResponseDto> getPositions() {
        List<Position> positions = positionRepository.findAll();
        return PositionMapper.toResponseDtoList(positions);
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
//        setPositionRoleOrThrow(position, dto.getRole());
        setSeniorityOrThrow(position, dto.getSeniority());
        setWorkModeOrThrow(position, dto.getWorkMode());
        setRate(position, dto.getRate());
        setRequiredSkillsForPosition(position, dto.getRequiredSkills());
        setOptionalSkills(position, dto.getOptionalSkills());
    }


    private void validateProjectAndUserAreRelated(Long projectId, Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = userService.getUserCompanyOrThrow(user);
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
        if (rateRequestDto.getMin() > rateRequestDto.getMax()) {
            throw new InvalidRequestException("Min rate cannot exceed max rate");
        }
        position.setRate(rateRepository.save(RateMapper.toRate(rateRequestDto)));
    }

    private void setRequiredSkillsForPosition(Position position, List<RequiredSkillsDto> dto) {
        if (dto == null || dto.isEmpty()) {
            throw new NotFoundException("RequiredSkillsDto is null or empty");
        }
        position.setRequiredSkills(getRequiredSkillsList(dto, position));
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
        if (requiredSkill.getExperience() != null) {
            Experience experience = experienceRepository.save(ExperienceMapper.toExperience(requiredSkill.getExperience()));
            requiredSkillResult.setExperience(experience);
        }
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

    private Position getPositionOrThrow(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Position with ID: " + id + " not found"));
    }


    private void activateProjectIfInactive(Project project) {
        if (project.getProjectStatus().equals(ProjectStatus.INACTIVE)) {
            project.setProjectStatus(ProjectStatus.ACTIVE);
            projectRepository.save(project);
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

    public void editPositionStatus(Long positionId, Long statusId, String customCloseReason, Authentication authentication) {
        userService.validateUserAndCompany(authentication);
        Position position = getPositionOrThrow(positionId);
        validateProjectAndUserAreRelated(position.getProject().getId(), authentication);

        setPositionStatusOrThrow(position, statusId);

        if (statusId.equals(5L) && (customCloseReason == null || customCloseReason.isEmpty() || customCloseReason.isBlank())) {
            throw new InvalidRequestException("Custom close reason must be entered");
        }
        position.setCustomCloseReason(customCloseReason);

        if (statusId == 1L) {
            activateProjectIfInactive(position.getProject());
            position.setCustomCloseReason(null);
        } else {
            deactivateProjectIfNoActivePositions(position.getProject());
        }

        updateProjectDateUpdated(position);

        positionRepository.save(position);
    }
}
