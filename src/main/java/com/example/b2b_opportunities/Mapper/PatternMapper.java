package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Response.PatternResponseDto;
import com.example.b2b_opportunities.Entity.Pattern;
import com.example.b2b_opportunities.Entity.Skill;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PatternMapper {
    public static PatternResponseDto toDto(Pattern pattern) {
        PatternResponseDto patternResponseDto = new PatternResponseDto();
        patternResponseDto.setId(pattern.getId());
        patternResponseDto.setName(pattern.getName());

        patternResponseDto.setParentId(null);
        if (pattern.getParent() != null) {
            patternResponseDto.setParentId(pattern.getParent().getId());
        }

        patternResponseDto.setSuggestedSkills(getSkillIds(pattern));
        return patternResponseDto;
    }

    public static List<PatternResponseDto> toDtoList(List<Pattern> patterns) {
        List<PatternResponseDto> patternResponseDtos = new ArrayList<>();
        for (Pattern p : patterns) {
            patternResponseDtos.add(toDto(p));
        }
        return patternResponseDtos;
    }

    private static List<Long> getSkillIds(Pattern pattern) {
        List<Long> skills = new ArrayList<>();
        for (Skill skill : pattern.getSuggestedSkills()) {
            skills.add(skill.getId());
        }
        return skills;
    }
}
