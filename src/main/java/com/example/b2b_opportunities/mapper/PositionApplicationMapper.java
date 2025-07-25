package com.example.b2b_opportunities.mapper;

import com.example.b2b_opportunities.dto.responseDtos.PositionApplicationResponseDto;
import com.example.b2b_opportunities.entity.PositionApplication;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PositionApplicationMapper {

    public static PositionApplicationResponseDto toPositionApplicationResponseDto(PositionApplication pa) {
        return PositionApplicationResponseDto.builder()
                .id(pa.getId())
                .companyId(pa.getTalentCompany().getId())
                .positionId(pa.getPosition().getId())
                .talentId(pa.getTalent() != null ? pa.getTalent().getId() : null)
                .applicationStatus(pa.getApplicationStatus().toString())
                .applicationDateTime(pa.getApplicationDateTime())
                .lastUpdateDateTime(pa.getLastUpdateDateTime())
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
