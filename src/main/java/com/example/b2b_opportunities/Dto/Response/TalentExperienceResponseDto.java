package com.example.b2b_opportunities.Dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TalentExperienceResponseDto {
    private List<SkillExperienceResponseDto> skillExperienceResponseDtoList;
    private int totalTime; //in months
    private Long patternId;
    private Long seniorityId;
}
