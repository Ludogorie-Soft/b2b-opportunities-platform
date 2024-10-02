package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequestDto extends ProjectEditRequestDto {
    @NotNull
    private Long companyId;
}
