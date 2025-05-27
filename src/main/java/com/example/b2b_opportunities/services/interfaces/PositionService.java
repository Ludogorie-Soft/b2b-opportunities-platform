package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.dto.requestDtos.PositionEditRequestDto;
import com.example.b2b_opportunities.dto.requestDtos.PositionRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.PositionResponseDto;
import com.example.b2b_opportunities.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.Set;

public interface PositionService {
    PositionResponseDto createPosition(PositionRequestDto dto, Authentication authentication);

    PositionResponseDto editPosition(Long id, PositionEditRequestDto dto, Authentication authentication);

    void deletePosition(Long id, Authentication authentication);

    PositionResponseDto getPosition(Authentication authentication, Long id);

    Page<PositionResponseDto> getPositions(Authentication authentication, int offset, int pageSize, String sort, boolean ascending, Integer rate,
                                           Set<Long> workModes,
                                           Set<Long> skills, Boolean isPartnerOnly);

    void editPositionStatus(Long positionId, Long statusId, String customCloseReason, Authentication authentication);

    Position getPositionOrThrow(Long id);
}
