package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Request.TalentRequestDto;
import com.example.b2b_opportunities.Dto.Response.SkillExperienceResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentExperienceResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentResponseDto;
import com.example.b2b_opportunities.Entity.Location;
import com.example.b2b_opportunities.Entity.SkillExperience;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.TalentExperience;
import com.example.b2b_opportunities.Entity.WorkMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class TalentMapper {
    public static Talent toEntity(TalentRequestDto dto) {
        return Talent.builder()
                .description(dto.getDescription())
                .isActive(dto.isActive())
                .build();
    }

    public static TalentResponseDto toResponseDto(Talent talent) {
        return TalentResponseDto.builder()
                .id(talent.getId())
                .isActive(talent.isActive())
                .maxRate(talent.getMaxRate())
                .minRate(talent.getMinRate())
                .companyId(talent.getCompany().getId())
                .description(talent.getDescription())
                .workModes(getWorkModeIds(talent.getWorkModes()))
                .locations(getLocationIds(talent.getLocations()))
                .experience(toTalentExperienceResponseDto(talent.getTalentExperience()))
                .build();
    }

    private static List<Long> getWorkModeIds(Set<WorkMode> workModes) {
        if (workModes.isEmpty()) {
            return new ArrayList<>();
        }
        return workModes.stream().map(WorkMode::getId).toList();
    }

    private static List<Long> getLocationIds(Set<Location> locations) {
        if (locations.isEmpty()) {
            return new ArrayList<>();
        }
        return locations.stream().map(Location::getId).toList();
    }

    public static TalentExperienceResponseDto toTalentExperienceResponseDto(TalentExperience talentExperience) {
        return TalentExperienceResponseDto.builder()
                .skills(toSkillExperienceResponseDtoList(talentExperience.getSkillExperienceList()))
                .totalTime(talentExperience.getTotalTime() != null ? talentExperience.getTotalTime() : 0)
                .patternId(talentExperience.getPattern().getId())
                .seniorityId(talentExperience.getSeniority().getId())
                .build();
    }

    public static List<SkillExperienceResponseDto> toSkillExperienceResponseDtoList(List<SkillExperience> skillExperienceList) {
        List<SkillExperienceResponseDto> responseDtoList = new ArrayList<>();
        for (SkillExperience se : skillExperienceList) {
            SkillExperienceResponseDto responseDto = SkillExperienceResponseDto.builder()
                    .skillId(se.getSkill().getId())
                    .months(se.getMonths())
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }
}
