package com.example.b2b_opportunities.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TalentResponseDto {
    private Long id;
    private boolean isActive;
    private Long companyId;
    private String description;
    private List<Long> workModes;
    private List<Long> locations;
    private TalentExperienceResponseDto experience;
    private Integer minRate;
    private Integer maxRate;
}
