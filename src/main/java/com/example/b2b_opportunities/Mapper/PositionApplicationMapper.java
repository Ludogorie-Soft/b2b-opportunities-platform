package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Response.PositionApplicationResponseDto;
import com.example.b2b_opportunities.Entity.PositionApplication;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PositionApplicationMapper {

    public static PositionApplicationResponseDto toPositionApplicationResponseDto(PositionApplication pa) {
        return PositionApplicationResponseDto.builder()
                .id(pa.getId())
                .positionId(pa.getPosition().getId())
                .talentId(pa.getTalent().getId())
                .applicationStatus(pa.getApplicationStatus().toString())
                .applicationDateTime(pa.getApplicationDateTime())
                .rate(pa.getRate())
                .availableFrom(pa.getAvailableFrom())
                .build();
    }

    public static List<PositionApplicationResponseDto> toPositionApplicationDtoList(List<PositionApplication> paList) {
        List<PositionApplicationResponseDto> positionApplicationDtoList = new ArrayList<>();
        for (PositionApplication pa : paList) {
            positionApplicationDtoList.add(toPositionApplicationResponseDto(pa));
        }
        return positionApplicationDtoList;
    }
}
