package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Response.RequiredSkillResponseDto;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RequiredSkillMapper {
    public static List<RequiredSkillResponseDto> toRequiredSkillResponseDtoList(List<RequiredSkill> requiredSkills){
        List<RequiredSkillResponseDto> responseDtoList = new ArrayList<>();
        for(RequiredSkill skill: requiredSkills){
            responseDtoList.add(RequiredSkillResponseDto.builder()
                    .skillId(skill.getId())
                    .experience(ExperienceMapper.toExperienceResponseDto(skill.getExperience()))
                    .build());
        }
        return responseDtoList;
    }
}