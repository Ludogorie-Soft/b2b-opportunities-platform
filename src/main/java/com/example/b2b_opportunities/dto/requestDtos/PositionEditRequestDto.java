package com.example.b2b_opportunities.dto.requestDtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionEditRequestDto extends PositionRequestDto {

    @NotNull
    private Long statusId;
}
