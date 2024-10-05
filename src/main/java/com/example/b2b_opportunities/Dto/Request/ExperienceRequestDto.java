package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExperienceRequestDto {
    @Min(0)
    private Integer months;
    @Min(0)
    private Integer years;
}
