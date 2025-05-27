package com.example.b2b_opportunities.dto.requestDtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TalentExperienceRequestDto {
    private List<SkillExperienceRequestDto> skills;
    @NotNull
    private Long patternId;
    @NotNull
    private Long seniorityId;
    private Integer totalTime;
}
