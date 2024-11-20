package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillExperienceRequestDto {
    private Long skillId;

    @Min(0)
    @Max(600)
    private Integer experience;
}
