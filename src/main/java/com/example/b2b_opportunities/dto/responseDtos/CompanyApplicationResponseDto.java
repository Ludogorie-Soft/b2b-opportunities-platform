package com.example.b2b_opportunities.dto.responseDtos;

import com.example.b2b_opportunities.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyApplicationResponseDto {
    private Long positionId;
    private Long companyId;
    private Set<Long> applicationIds;
    private ApplicationStatus overallStatus;
    private LocalDateTime lastUpdateDateTime;
}
