package com.example.b2b_opportunities.Dto.Response;

import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyResponseDto {
    private Long id;
    private String name;
    private String email;
    private CompanyType companyType;
    private Domain domain;
    private String emailVerification;
    private String website;
    private String linkedIn;
    private String image;
    private String banner;
    private String description;
    private List<Long> skills;
}
