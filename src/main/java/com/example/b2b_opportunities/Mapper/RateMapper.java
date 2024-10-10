package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Request.RateRequestDto;
import com.example.b2b_opportunities.Dto.Response.RateResponseDto;
import com.example.b2b_opportunities.Entity.Rate;
import org.springframework.stereotype.Component;

@Component
public class RateMapper {
    public static Rate toRate(RateRequestDto requestDto){
        return Rate.builder()
                .min(requestDto.getMin())
                .max(requestDto.getMax())
                .currency(requestDto.getCurrency())
                .build();
    }

    public static RateResponseDto toRateResponseDto(Rate rate){
        return RateResponseDto.builder()
                .min(rate.getMin())
                .max(rate.getMax())
                .currency(rate.getCurrency())
                .build();
    }
}
