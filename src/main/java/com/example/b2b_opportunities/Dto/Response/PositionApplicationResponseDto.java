package com.example.b2b_opportunities.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionApplicationResponseDto {
    private Long id;
    private Long positionId;
    private Long talentId;
    private String applicationStatus;
    private LocalDateTime applicationDateTime;
    private LocalDateTime availableFrom;
    private int rate;
}
