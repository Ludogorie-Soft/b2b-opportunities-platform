package com.example.b2b_opportunities.dto.requestDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequiredSkillsDto {
    @NotNull
    private Long skillId;
    @Valid
    private Integer months;
}