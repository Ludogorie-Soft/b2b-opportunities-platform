package com.example.b2b_opportunities.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartnerGroupResponseDto {
    private Long id;
    private String name;
    private List<CompanyPublicResponseDto> companies;
}
