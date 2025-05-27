package com.example.b2b_opportunities.dto.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TalentExperienceResponseDto {
    private List<SkillExperienceResponseDto> skills;
    private int totalTime;
    private Long patternId;
    private Long seniorityId;
}
