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
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponseDto {
    private Long id;
    private Long projectId;
    private Long role;
    private boolean isActive;
    private Short seniority;
    private List<String> workMode;
    private RateResponseDto rate;
    private List<RequiredSkillResponseDto> requiredSkills;
    private List<Long> optionalSkills;
    private Integer minYearsExperience;
    private String location;
    private int hoursPerWeek;
    private List<String> responsibilities;
    private String hiringProcess;
    private String description;
}