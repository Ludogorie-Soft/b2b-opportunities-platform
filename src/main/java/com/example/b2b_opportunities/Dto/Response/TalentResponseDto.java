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
    private Long companyId;
    private String description;
    private boolean isActive;
    private String residence;
    private List<TalentExperienceResponseDto> talentExperienceResponseDtoList;
}
