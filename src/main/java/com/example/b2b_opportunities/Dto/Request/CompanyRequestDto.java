package com.example.b2b_opportunities.Dto.Request;

import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@Setter
public class CompanyRequestDto {

    @NotEmpty
    private String name;

    @NotEmpty
    @Email
    private String email;

    @NotNull
    private Long companyTypeId;

    @NotEmpty
    @URL
    private String website;

    private Long domainId;

    @URL
    private String linkedIn;

    private String description;

    private List<Long> skills;
}
