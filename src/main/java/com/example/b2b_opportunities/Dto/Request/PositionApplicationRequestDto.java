package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionApplicationRequestDto {

    @NotNull
    private Long positionId;

    @NotNull
    private Long talentId;
}
