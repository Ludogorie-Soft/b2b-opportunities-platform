package com.example.b2b_opportunities.dto.requestDtos;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateRequestDto {

    //    @NotNull
    @Min(0)
    private Integer min;

    @Min(0)
    private Integer max;

//    @NotNull
//    private Long currencyId;
}
