package com.example.b2b_opportunities.Dto.Request;

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

    public void setName(@NotEmpty String name) {
        if (name != null) {
            this.name = name.trim().replaceAll("\\s+", " ");
        }
    }

    public void setEmail(@NotEmpty @Email String email) {
        if (email != null) {
            this.email = email.toLowerCase().trim();
        }
    }
}
