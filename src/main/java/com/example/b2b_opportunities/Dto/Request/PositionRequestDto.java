package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PositionRequestDto {
    @NotNull
    private Long projectId;

//    @NotNull
//    private Long role;
    @NotNull
    private Long patternId;

    @NotNull
    private Long seniority;

    @NotNull
    private List<Long> workMode;

    @NotNull
    @Valid
    private RateRequestDto rate;

    @NotNull
    @Valid
    private List<RequiredSkillsDto> requiredSkills;

    private List<Long> optionalSkills;

    @Min(0)
    private Integer minYearsExperience;

    private Long location;

    @Min(0)
    @Max(168)
    private int hoursPerWeek;

    private List<String> responsibilities;

    private String hiringProcess;

    @NotBlank
    private String description;

}
