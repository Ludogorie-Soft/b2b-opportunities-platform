package com.example.b2b_opportunities.Dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TalentRequestDto {
    private TalentExperienceRequestDto talentExperienceRequestDto;
    private String description;
    private boolean isActive;
    private String residence;

}
