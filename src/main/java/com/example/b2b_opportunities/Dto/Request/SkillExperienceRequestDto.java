package com.example.b2b_opportunities.Dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillExperienceRequestDto {
    private Long skillId;
    private Integer years;
    private Integer months;
}
