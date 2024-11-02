package com.example.b2b_opportunities.Dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TalentResponseDto {
    private Long id;
    private Long companyId;
    private String description;
    private boolean isActive;
    private String residence;
    private List<TalentExperienceResponseDto> talentExperienceResponseDtoList;
}
