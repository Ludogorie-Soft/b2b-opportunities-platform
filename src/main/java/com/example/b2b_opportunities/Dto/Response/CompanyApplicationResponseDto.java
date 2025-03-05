package com.example.b2b_opportunities.Dto.Response;

import com.example.b2b_opportunities.Static.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyApplicationResponseDto {
    private Long positionId;
    private Set<Long> applicationIds;
    private ApplicationStatus overallStatus;
}
