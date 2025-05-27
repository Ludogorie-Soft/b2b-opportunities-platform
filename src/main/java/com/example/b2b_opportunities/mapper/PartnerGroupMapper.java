package com.example.b2b_opportunities.mapper;

import com.example.b2b_opportunities.dto.responseDtos.PartnerGroupResponseDto;
import com.example.b2b_opportunities.entity.PartnerGroup;

import java.util.ArrayList;

public class PartnerGroupMapper {
    public static PartnerGroupResponseDto toPartnerGroupResponseDto(PartnerGroup partnerGroup){
        return PartnerGroupResponseDto.builder()
                .id(partnerGroup.getId())
                .name(partnerGroup.getName())
                .companies(CompanyMapper.toCompanyPublicResponseDtoList(new ArrayList<>(partnerGroup.getPartners())))
                .build();
    }
}
