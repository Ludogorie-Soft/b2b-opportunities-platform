package com.example.b2b_opportunities.Dto.Response;

import lombok.Builder;

import java.util.List;

@Builder
public class CompanyResponseDto {
    private Long id;
    private String name;
    private String email;
    private String companyType;
    private String website;
    private String image;
    private String emailVerification;
    private String domain;
    private String linkedIn;
    private String banner;
    private String description;
    private List<Long> skills;
    private List<Long> users;

}
