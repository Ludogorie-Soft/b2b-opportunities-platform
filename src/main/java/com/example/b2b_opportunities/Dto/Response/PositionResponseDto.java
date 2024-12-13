package com.example.b2b_opportunities.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PositionResponseDto {
    private Long id;
    private Long projectId;
    private Long patternId;
    private Short seniority;
    private Set<Long> workMode;
    private RateResponseDto rate;
    private List<RequiredSkillResponseDto> requiredSkills;
    private List<Long> optionalSkills;
    private Integer minYearsExperience;
    private Long location;
    private int hoursPerWeek;
    private List<String> responsibilities;
    private String hiringProcess;
    private String description;
    private Long statusId;
    private String customCloseReason;
    private Long views;
    private Long applications;
    private Long approvedApplications;
}