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
                .talentExperienceResponseDtoList(toTalentExperienceResponseDtoList(talent.getExperienceList()))
                .build();
    }

    public static List<TalentExperienceResponseDto> toTalentExperienceResponseDtoList(List<TalentExperience> talentExperienceList) {
        List<TalentExperienceResponseDto> responseDtoList = new ArrayList<>();
        for (TalentExperience te : talentExperienceList) {
            TalentExperienceResponseDto responseDto = TalentExperienceResponseDto.builder()
                    .skillExperienceResponseDtoList(toSkillExperienceResponseDtoList(te.getSkillExperienceList()))
                    .totalTime(te.getTotalTime())
                    .patternId(te.getPattern().getId())
                    .seniorityId(te.getSeniority().getId())
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
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
