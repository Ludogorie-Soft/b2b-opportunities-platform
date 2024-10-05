package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Response.ExperienceResponseDto;
import com.example.b2b_opportunities.Dto.Response.RequiredSkillResponseDto;
import com.example.b2b_opportunities.Entity.Experience;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequiredSkillMapper {
    public static List<RequiredSkillResponseDto> toRequiredSkillResponseDtoList(List<RequiredSkill> requiredSkills) {
        return requiredSkills.stream().map(skill -> {
            RequiredSkillResponseDto responseDto = new RequiredSkillResponseDto();
            responseDto.setSkillId(skill.getId());
            if (skill.getExperience() != null) {
                Experience experience = skill.getExperience();
                ExperienceResponseDto experienceResponseDto = new ExperienceResponseDto();
                if (experience.getYears() != null) {
                    experienceResponseDto.setYears(experience.getYears());
                }
                if (experience.getMonths() != null) {
                    experienceResponseDto.setMonths(experience.getMonths());
                }
                responseDto.setExperience(experienceResponseDto);
            }
            return responseDto;
        }).collect(Collectors.toList());
    }
}