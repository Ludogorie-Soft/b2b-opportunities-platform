package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Request.TalentRequestDto;
import com.example.b2b_opportunities.Dto.Response.SkillExperienceResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentExperienceResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentResponseDto;
import com.example.b2b_opportunities.Entity.SkillExperience;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.TalentExperience;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TalentMapper {
    public static Talent toEntity(TalentRequestDto dto) {
        return Talent.builder()
                .description(dto.getDescription())
                .isActive(dto.isActive())
                .residence(dto.getResidence())
                .build();
    }

    public static TalentResponseDto toResponseDto(Talent talent) {
        return TalentResponseDto.builder()
                .id(talent.getId())
                .companyId(talent.getCompany().getId())
                .description(talent.getDescription())
                .isActive(talent.isActive())
                .residence(talent.getResidence())
                .experience(toTalentExperienceResponseDto(talent.getTalentExperience()))
                .build();
    }

    public static TalentExperienceResponseDto toTalentExperienceResponseDto(TalentExperience talentExperience) {
        return TalentExperienceResponseDto.builder()
                .skills(toSkillExperienceResponseDtoList(talentExperience.getSkillExperienceList()))
                .totalTime(talentExperience.getTotalTime())
                .patternId(talentExperience.getPattern().getId())
                .seniorityId(talentExperience.getSeniority().getId())
                .build();
    }

    public static List<SkillExperienceResponseDto> toSkillExperienceResponseDtoList(List<SkillExperience> skillExperienceList) {
        List<SkillExperienceResponseDto> responseDtoList = new ArrayList<>();
        for (SkillExperience se : skillExperienceList) {
            SkillExperienceResponseDto responseDto = SkillExperienceResponseDto.builder()
                    .skillId(se.getSkill().getId())
                    .years(se.getExperience().getYears())
                    .months(se.getExperience().getMonths())
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }
}
