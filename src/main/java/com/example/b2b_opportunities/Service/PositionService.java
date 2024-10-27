package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Request.RateRequestDto;
import com.example.b2b_opportunities.Dto.Request.RequiredSkillsDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Experience;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionStatus;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Entity.WorkMode;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.ExperienceMapper;
import com.example.b2b_opportunities.Mapper.PositionMapper;
import com.example.b2b_opportunities.Mapper.RateMapper;
import com.example.b2b_opportunities.Repository.ExperienceRepository;
import com.example.b2b_opportunities.Repository.PositionRepository;
import com.example.b2b_opportunities.Repository.PositionRoleRepository;
import com.example.b2b_opportunities.Repository.PositionStatusRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Repository.RateRepository;
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
    private final PositionRoleRepository positionRoleRepository;
    private final SkillRepository skillRepository;
    private final PositionRepository positionRepository;
    private final RateRepository rateRepository;
    private final ExperienceRepository experienceRepository;
    private final WorkModeRepository workModeRepository;
    private final PositionStatusRepository positionStatusRepository;
    private final AdminService adminService;

    public PositionResponseDto createPosition(PositionRequestDto dto, Authentication authentication) {
        validateUserAndCompany(authentication);
        validateProjectAndUserAreRelated(dto.getProjectId(), authentication);

        Position position = PositionMapper.toPosition(dto);

        setProjectOrThrow(position, dto.getProjectId());
        setPositionFields(position, dto);
        updateProjectDateUpdated(position);
        activateProjectIfInactive(position.getProject());
        position.setStatus(positionStatusRepository.findById(1L).orElseThrow());

        return PositionMapper.toResponseDto(positionRepository.save(position));
    }

    public PositionResponseDto editPosition(Long id, PositionRequestDto dto, Authentication authentication) {
        validateUserAndCompany(authentication);
        Position position = getPositionOrThrow(id);

        validateProjectAndUserAreRelated(position.getProject().getId(), authentication);

        position.setMinYearsExperience(dto.getMinYearsExperience());
        position.setHoursPerWeek(dto.getHoursPerWeek());
        position.setResponsibilities(dto.getResponsibilities());
        position.setHiringProcess(dto.getHiringProcess());
        position.setDescription(dto.getDescription());

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
        List<PositionResponseDto> positionResponseDtoList = new ArrayList<>();
        for (Position position : positions) {
            positionResponseDtoList.add(PositionMapper.toResponseDto(position));
        }
        return positionResponseDtoList;
    }

    private void updateProjectDateUpdated(Position position) {
        Project project = position.getProject();
        project.setDateUpdated(LocalDateTime.now());
        projectRepository.save(project);
    }

    private void setPositionFields(Position position, PositionRequestDto dto) {
        setPositionRoleOrThrow(position, dto.getRole());
        setSeniorityOrThrow(position, dto.getSeniority());
        setWorkModeOrThrow(position, dto.getWorkMode());
        setRate(position, dto.getRate());
        setRequiredSkillsForPosition(position, dto.getRequiredSkills());
        setOptionalSkills(position, dto.getOptionalSkills());
    }

    private void validateUserAndCompany(Authentication authentication) {
        User currentUser = getUserOrThrow(authentication);
        getUserCompanyOrThrow(currentUser);
    }

    private User getUserOrThrow(Authentication authentication) {
        if (authentication == null) {
            throw new AuthenticationFailedException("User not authenticated");
        }
        return adminService.getCurrentUserOrThrow(authentication);
    }

    private Company getUserCompanyOrThrow(User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new NotFoundException("No company is associated with user " + user.getUsername());
        }
        return company;
    }

    private void validateProjectAndUserAreRelated(Long projectId, Authentication authentication) {
        User user = getUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(user);
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

    private void setPositionRoleOrThrow(Position position, Long positionRoleId) {
        position.setRole(positionRoleRepository.findById(positionRoleId)
                .orElseThrow(() -> new NotFoundException("Role with ID: " + positionRoleId + " was not found")));
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
            Skill skill = skillRepository.findById(requiredSkill.getSkillId())
                    .orElseThrow(() -> new NotFoundException("Skill with ID: " + requiredSkill.getSkillId() + " was not found"));
            RequiredSkill requiredSkillResult = new RequiredSkill();
            requiredSkillResult.setPosition(position);
            requiredSkillResult.setSkill(skill);
            if (requiredSkill.getExperienceRequestDto() != null) {
                Experience experience = experienceRepository.save(ExperienceMapper
                        .toExperience(requiredSkill.getExperienceRequestDto()));
                requiredSkillResult.setExperience(experience);
            }
            requiredSkillList.add(requiredSkillResult);
        }
        return requiredSkillList;
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

    public void editPositionStatus(Long id, Long statusId, Authentication authentication) {
        validateUserAndCompany(authentication);

        Position position = getPositionOrThrow(id);

        validateProjectAndUserAreRelated(position.getProject().getId(), authentication);

        setPositionStatusOrThrow(position, statusId);

        if (statusId == 1L) {
            activateProjectIfInactive(position.getProject());
        } else {
            deactivateProjectIfNoActivePositions(position.getProject());
        }

        updateProjectDateUpdated(position);

        positionRepository.save(position);
    }
}
