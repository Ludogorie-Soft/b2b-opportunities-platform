package com.example.b2b_opportunities.Dto.Request;

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

    @NotNull
    private Long roleId;

    @NotNull
    private Boolean isActive;

    @NotNull
    private Long seniorityId;

    @NotNull
    private List<Long> workModeIds;

    @NotNull
    private RateRequestDto rate;

    @NotNull
    private List<RequiredSkillsDto> requiredSkillsList;

    private List<Long> optionalSkillsList;

    @Min(0)
    private Integer minYearsExperience;

    private Long locationId;

    @Min(0)
    @Max(168)
    private int hoursPerWeek;

    private List<String> responsibilities;

    private String hiringProcess;

    @NotBlank
    private String description;
}
