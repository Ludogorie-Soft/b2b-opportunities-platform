package com.example.b2b_opportunities.dto.responseDtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TalentStatsDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer positionsApplied;
    private LocalDateTime lastAppliedAt;
}
