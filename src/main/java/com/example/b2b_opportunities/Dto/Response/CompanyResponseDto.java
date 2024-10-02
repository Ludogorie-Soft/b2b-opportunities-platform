package com.example.b2b_opportunities.Dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class CompanyResponseDto {
    private Long id;
    private String name;
    private String email;
    private String companyType;
    private String domain;
    private String emailVerification;
    private String website;
    private String linkedIn;
    private String image;
    private String banner;
    private String description;
    private List<Long> skills;
}
