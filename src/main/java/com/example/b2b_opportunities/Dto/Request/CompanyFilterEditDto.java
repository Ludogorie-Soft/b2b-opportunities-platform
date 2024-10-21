package com.example.b2b_opportunities.Dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyFilterEditDto extends CompanyFilterRequestDto {
    private Boolean isEnabled;
}
