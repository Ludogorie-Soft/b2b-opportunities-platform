package com.example.b2b_opportunities.Dto.Request;

import com.example.b2b_opportunities.Entity.PositionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectStatusUpdateRequestDto {
    @NotNull
    private PositionStatus status;
}