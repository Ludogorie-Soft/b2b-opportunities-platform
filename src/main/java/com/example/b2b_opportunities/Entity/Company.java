package com.example.b2b_opportunities.Entity;

import com.example.b2b_opportunities.Static.EmailVerification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "companies")
@RequiredArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    private String name;
    @NotNull
    private String email;

    @NotNull
    @ManyToMany
    @JoinTable(
            name = "companies_company_types",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "company_type_id")
    )
    private Set<CompanyType> companyTypeList = new HashSet<>();

    @NotNull
    private String website;

    @NotNull
    private String image;

    private EmailVerification emailVerification;

    @ManyToOne
    @JoinColumn(name = "domain_id")
    private Domain domain;

    private String linkedIn;

    private String banner;

    @OneToMany(mappedBy = "company")
    private List<User> users;

    private String description;

    @ManyToMany
    @JoinTable(
            name = "companies_skills",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();
}