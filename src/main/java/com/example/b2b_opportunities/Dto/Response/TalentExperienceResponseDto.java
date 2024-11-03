package com.example.b2b_opportunities.Dto.Response;

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
    private int totalTime; //in months
    private Long patternId;
    private Long seniorityId;
}
