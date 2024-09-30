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
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotEmpty;
    import jakarta.validation.constraints.NotNull;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import org.hibernate.validator.constraints.URL;

    import java.util.ArrayList;
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

        @NotEmpty
        @URL
        private String website;

        @NotEmpty
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
                name = "company_skills",
                joinColumns = @JoinColumn(name = "company_id"),
                inverseJoinColumns = @JoinColumn(name = "skill_id")
        )
        private Set<Skill> skills;
    }