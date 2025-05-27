package com.example.b2b_opportunities.dto.requestDtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PartnerGroupRequestDto {
    private String name;
    private Set<Long> companyIds;
}
