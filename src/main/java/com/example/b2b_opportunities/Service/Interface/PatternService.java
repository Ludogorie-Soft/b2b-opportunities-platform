package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Dto.Request.PatternRequestDto;
import com.example.b2b_opportunities.Dto.Response.PatternResponseDto;
import com.example.b2b_opportunities.Entity.Skill;

import java.util.List;

public interface PatternService {
    PatternResponseDto get(Long id);

    List<PatternResponseDto> getAll();

    PatternResponseDto create(PatternRequestDto dto);

    PatternResponseDto update(PatternRequestDto dto);

    void delete(Long id);

    // TODO: move this method to a better place (make SET)
    List<Skill> getAllSkillsIfSkillIdsExist(List<Long> skillList);

    List<Skill> getAllAssignableSkillsIfSkillIdsExist(List<Long> skillList);

    List<Skill> getAllSkillsIfSkillIdsExist(List<Long> skillIds, boolean getAssignableOnly);
}
