package com.example.b2b_opportunities.Dto.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TalentExperienceRequestDto {
    private List<SkillExperienceRequestDto> skillExperienceRequestDtoList;
    //auto-calculated totalTime
    private Long patternId;
    private Long seniorityId;
}
