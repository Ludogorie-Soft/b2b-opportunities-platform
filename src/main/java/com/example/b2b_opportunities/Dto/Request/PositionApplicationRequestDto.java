package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.NotNull;
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
public class PositionApplicationRequestDto {

    @NotNull
    private Long positionId;

    private Long talentId;

    @NotNull
    private int rate;

    @NotNull
    private LocalDateTime availableFrom;
}
