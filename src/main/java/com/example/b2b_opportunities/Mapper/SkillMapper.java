package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Response.SkillResponseDto;
import com.example.b2b_opportunities.Entity.Skill;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class SkillMapper {
    public static SkillResponseDto toResponseDto(Skill skill) {
        SkillResponseDto skillResponseDTO = new SkillResponseDto();

        skillResponseDTO.setId(skill.getId());
        skillResponseDTO.setName(skill.getName());
        skillResponseDTO.setAssignable(skill.getAssignable());
        skillResponseDTO.setParent(null);

        // Set the parent to DTO
        if (skill.getParent() != null) {
            skillResponseDTO.setParent(toResponseDto(skill.getParent()));
        }

        skillResponseDTO.setImageType(skill.getImageType());
        skillResponseDTO.setImageBase64(null);

        if (skill.getImage() != null) {
            String base64Image = Base64.getEncoder().encodeToString(skill.getImage());
            skillResponseDTO.setImageBase64(base64Image);
        }

        return skillResponseDTO;
    }

    public static List<SkillResponseDto> toSkillResponseDtoList(List<Skill> skills) {
        List<SkillResponseDto> skillResponseDtoList = new ArrayList<>();
        for (Skill skill : skills) {
            skillResponseDtoList.add(toResponseDto(skill));
        }
        return skillResponseDtoList;
    }
}
