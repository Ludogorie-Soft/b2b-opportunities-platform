package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequiredSkillsDto {
    @NotNull
    private Long skillId;
    private ExperienceRequestDto experienceRequestDto;
}