package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Response.RequiredSkillResponseDto;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequiredSkillMapper {
    public static List<RequiredSkillResponseDto> toRequiredSkillResponseDtoList(List<RequiredSkill> requiredSkills) {
        return requiredSkills.stream().map(s -> {
            RequiredSkillResponseDto dto = new RequiredSkillResponseDto();
            dto.setSkillId(s.getSkill().getId());

            // Set Experience
            if (s.getMonths() != null) dto.setMonths(s.getMonths());
            return dto;
        }).collect(Collectors.toList());
    }
}