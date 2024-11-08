package com.example.b2b_opportunities.Entity;

import com.example.b2b_opportunities.Static.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "projects")
@RequiredArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonManagedReference
    private Company company;

    private LocalDateTime datePosted;
    private LocalDateTime dateUpdated;
    private LocalDateTime expiryDate;

    @NotEmpty
    private String name;

    private LocalDate startDate;
    private LocalDate endDate; // YYYY-MM-DD
    private Integer duration; // months

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<Position> positions;
    private String description;

    @Enumerated(EnumType.STRING)
    private ProjectStatus projectStatus;

    private boolean isPartnerOnly;

    @ManyToMany
    @JoinTable(
            name = "project_partner_group",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "partner_group_id")
    )
    private List<PartnerGroup> partnerGroupList;
    public void setName(@NotEmpty String name) {
        this.name = name.strip();
    }
}