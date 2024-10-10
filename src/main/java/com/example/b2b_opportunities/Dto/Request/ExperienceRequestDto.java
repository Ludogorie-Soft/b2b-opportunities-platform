package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExperienceRequestDto {
    @Min(0)
    @Max(11)
    private Integer months;

    @Min(0)
    @Max(100)
    private Integer years;
}
