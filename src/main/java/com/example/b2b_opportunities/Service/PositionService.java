package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Request.RateRequestDto;
import com.example.b2b_opportunities.Dto.Request.RequiredSkillsDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Experience;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.InvalidInputException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Mapper.ExperienceMapper;
import com.example.b2b_opportunities.Mapper.PositionMapper;
import com.example.b2b_opportunities.Mapper.RateMapper;
import com.example.b2b_opportunities.Repository.ExperienceRepository;
import com.example.b2b_opportunities.Repository.PositionRepository;
import com.example.b2b_opportunities.Repository.PositionRoleRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Repository.RateRepository;
import com.example.b2b_opportunities.Repository.SeniorityRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.WorkMode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PositionService {
    private final CompanyService companyService;
    private final ProjectRepository projectRepository;
    private final SeniorityRepository seniorityRepository;
    private final PositionRoleRepository positionRoleRepository;
    private final SkillRepository skillRepository;
    private final PositionRepository positionRepository;
    private final RateRepository rateRepository;
    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;
    public PositionResponseDto createPosition(PositionRequestDto dto, Authentication authentication) {
        validateUserAndCompany(authentication);
        Position position = PositionMapper.toPosition(dto);

        setProjectOrThrow(position, dto.getProjectId());
        setPositionFields(position, dto);

        return PositionMapper.toResponseDto(positionRepository.save(position));
    }

    private void setPositionFields(Position position, PositionRequestDto dto){
        setPositionRoleOrThrow(position, dto.getRoleId());
        setSeniorityOrThrow(position, dto.getSeniorityId());
        setWorkModeOrThrow(position, dto.getWorkModeIds());
        setRate(position, dto.getRate());
        setRequiredSkills(position, dto.getRequiredSkillsList());
        setOptionalSkills(position, dto.getOptionalSkillsList());
    }

    private void validateUserAndCompany(Authentication authentication) {
        if (authentication == null) {
            throw new AuthenticationFailedException("User not authenticated");
        }
        User currentUser = companyService.getCurrentUser(authentication);
        Company company = currentUser.getCompany();
        if (company == null) {
            throw new NotFoundException("No company is associated with user " + currentUser.getUsername());
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

    private void setSeniorityOrThrow(Position position, Long seniorityId) {
        position.setSeniority(seniorityRepository.findById(seniorityId)
                .orElseThrow(() -> new NotFoundException("Seniority with ID: " + seniorityId + " was not found")));
    }

    private void setWorkModeOrThrow(Position position, List<Long> workModeIdsList) {
        Set<WorkMode> workModes = new HashSet<>();
        for (Long workModeId : workModeIdsList) {
            boolean found = false;
            for (WorkMode value : WorkMode.values()) {
                if (value.getId() == workModeId) {
                    workModes.add(value);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new NotFoundException("Invalid WorkMode ID: " + workModeId);
            }
        }
        position.setWorkMode(workModes);
    }

    private void setRate(Position position, RateRequestDto rateRequestDto) {
        if(rateRequestDto.getMin()>rateRequestDto.getMax()) throw new InvalidInputException("Min rate cannot exceed max rate");
        position.setRate(rateRepository.save(RateMapper.toRate(rateRequestDto)));
    }

    private void setRequiredSkills(Position position, List<RequiredSkillsDto> dto) {
        if (dto == null || dto.isEmpty()) throw new NotFoundException("RequiredSkillsDto is null or empty");
        position.setRequiredSkills(getRequiredSkillsList(dto));
    }

    private void setOptionalSkills(Position position, List<Long> skills) {
        if (skills == null || skills.isEmpty()) {
            return;
        }
        position.setOptionalSkills(skillRepository.findAllById(skills));
    }

    private List<RequiredSkill> getRequiredSkillsList(List<RequiredSkillsDto> dto) {
        List<RequiredSkill> requiredSkillList = new ArrayList<>();
        for (RequiredSkillsDto requiredSkill : dto) {
            Skill skill = skillRepository.findById(requiredSkill.getSkillId())
                    .orElseThrow(() -> new NotFoundException("Skill with ID: " + requiredSkill.getSkillId() + " was not found"));
            RequiredSkill requiredSkillResult = new RequiredSkill();
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
}
