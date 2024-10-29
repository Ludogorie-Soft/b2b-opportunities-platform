package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateRequestDto {
    @NotNull
    @Min(0)
    private Integer min;
    @NotNull
    @Min(0)
    private Integer max;
    @NotNull
    private Long currencyId;  // TODO - create enum or class ? - We can make an ENUM having only BGN/EUR as currencies for the MVP ?
}
