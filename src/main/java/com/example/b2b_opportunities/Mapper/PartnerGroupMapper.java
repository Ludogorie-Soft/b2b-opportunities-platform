package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Response.PartnerGroupResponseDto;
import com.example.b2b_opportunities.Entity.PartnerGroup;

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
