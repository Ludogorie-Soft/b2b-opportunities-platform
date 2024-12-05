package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PositionApplicationRequestDto {

    @NotNull
    private Long positionId;

    private Long talentId;

    @NotNull
    private int rate;

    @NotNull
    private LocalDateTime availableFrom;
}
