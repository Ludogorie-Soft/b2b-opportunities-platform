package com.example.b2b_opportunities.Dto.Request;

import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Static.EmailVerification;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CompanyRequestDto {

    @NotEmpty
    private String name;
    @NotEmpty
    @Email
    private String email;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "company_type_id")
    private CompanyType companyType;
    @NotEmpty
    @URL
    private String website;
    @ManyToOne
    @JoinColumn(name = "domain_id")
    private Domain domain;
    private String linkedIn;
//    private List<Long> users; -> current user will be set automatically
    private String description;
    private Set<Long> skills;
}
