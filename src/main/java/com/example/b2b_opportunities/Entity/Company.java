package com.example.b2b_opportunities.Entity;

import com.example.b2b_opportunities.Static.EmailVerification;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@Table(name = "companies")
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotEmpty
    private String name;

    @NotEmpty
    @Email
    private String email;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "company_type_id")
    private CompanyType companyType;

    @URL
    private String website;

    @Enumerated(EnumType.STRING)
    private EmailVerification emailVerification;

    @ManyToOne
    @JoinColumn(name = "domain_id")
    private Domain domain;

    @Column(name = "linked_in")
    @URL
    private String linkedIn;

    @OneToMany(mappedBy = "company")
    private List<User> users;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "company_skills",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills;

    private String emailConfirmationToken;
    
    @OneToMany(mappedBy = "company")
    @JsonBackReference
    private List<Project> projects;

    @OneToMany(mappedBy = "company")
    private Set<Filter> filters;

    public void setName(@NotEmpty String name) {
        this.name = name.strip();
    }

    public void setEmail(@NotEmpty @Email String email) {
        this.email = email.strip().toLowerCase();
    }

    Set<Long> projectIdsNotified;
    @ManyToMany
    @JoinTable(
            name = "company_partners",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "partner_id")
    )
    private Set<Company> partners;
}