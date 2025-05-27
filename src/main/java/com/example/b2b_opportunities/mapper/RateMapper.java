package com.example.b2b_opportunities.mapper;

import com.example.b2b_opportunities.dto.requestDtos.RateRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.RateResponseDto;
import com.example.b2b_opportunities.entity.Rate;
import org.springframework.stereotype.Component;

@Component
public class RateMapper {
    public static Rate toRate(RateRequestDto requestDto) {
        return Rate.builder()
                .min(requestDto.getMin())
                .max(requestDto.getMax())
                .build();
    }

    public static RateResponseDto toRateResponseDto(Rate rate) {
        return RateResponseDto.builder()
                .min(rate.getMin())
                .max(rate.getMax())
                .currencyId(rate.getCurrency().getId())
                .build();
    }
}
