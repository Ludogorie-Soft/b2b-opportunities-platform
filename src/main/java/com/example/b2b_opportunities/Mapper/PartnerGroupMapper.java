package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Response.PartnerGroupResponseDto;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class PartnerGroupMapper {
    public static PartnerGroupResponseDto toPartnerGroupResponseDto(PartnerGroup partnerGroup){
        return PartnerGroupResponseDto.builder()
                .id(partnerGroup.getId())
                .name(partnerGroup.getName())
                .companies(CompanyMapper.toCompanyResponseDtoSet(partnerGroup.getPartners()))
                .build();
    }
}
