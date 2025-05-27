package com.example.b2b_opportunities.dto.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TalentPublicityResponseDto {
    private boolean isPublic;
    private Set<Long> partnerGroupIds;
}
