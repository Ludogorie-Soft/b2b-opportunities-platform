package com.example.b2b_opportunities.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "talents")
public class Talent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    private String description;

    @ManyToMany
    @JoinTable(name = "talent_work_modes",
            joinColumns = @JoinColumn(name = "talent_id"),
            inverseJoinColumns = @JoinColumn(name = "work_mode_id")
    )
    @Column(name = "work_mode")
    private Set<WorkMode> workModes;

    @ManyToOne
    @JoinColumn(name = "talent_experience_id")
    private TalentExperience talentExperience;

    @ManyToMany
    @JoinTable(name = "talent_locations",
            joinColumns = @JoinColumn(name = "talent_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    @Column(name = "location")
    private Set<Location> locations;
}
