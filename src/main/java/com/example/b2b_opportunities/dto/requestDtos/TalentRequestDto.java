package com.example.b2b_opportunities.dto.requestDtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TalentRequestDto {
    private boolean isActive;
    @NotNull
    private String description;
    private List<Long> workModes; //can be null
    private List<Long> locations; //can be null
    private TalentExperienceRequestDto experience;
    private Integer minRate;
    private Integer maxRate;
}
