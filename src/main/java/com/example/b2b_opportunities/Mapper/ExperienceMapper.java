package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Request.ExperienceRequestDto;
import com.example.b2b_opportunities.Dto.Response.ExperienceResponseDto;
import com.example.b2b_opportunities.Entity.Experience;
import org.springframework.stereotype.Component;

@Component
public class ExperienceMapper {

    public static Experience toExperience (ExperienceRequestDto dto){
        return Experience.builder()
                .months(dto.getMonths())
                .years(dto.getYears())
                .build();
    }

    public static ExperienceResponseDto toExperienceResponseDto(Experience experience){
        return ExperienceResponseDto.builder()
                .months(experience.getMonths())
                .years(experience.getYears())
                .build();
    }
}
