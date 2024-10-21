package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CompanyFilterRequestDto {
    @NotBlank
    private String name;

    @NotEmpty
    private Set<Long> skillIds;
}
