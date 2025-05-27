package com.example.b2b_opportunities.dto.requestDtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyFilterEditDto extends CompanyFilterRequestDto {
    private Boolean isEnabled;
}
