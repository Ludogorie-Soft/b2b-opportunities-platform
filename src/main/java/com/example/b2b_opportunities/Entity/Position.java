package com.example.b2b_opportunities.Entity;

import com.example.b2b_opportunities.Static.WorkMode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "positions")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private PositionRole role;

    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "seniority_id")
    private Seniority seniority;

    @ElementCollection(targetClass = WorkMode.class)
    @CollectionTable(name = "position_work_modes", joinColumns = @JoinColumn(name = "position_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode")
    private Set<WorkMode> workMode;

    @ManyToOne
    @JoinColumn(name = "rate_id")
    private Rate rate;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequiredSkill> requiredSkills;

    @OneToMany
    @JoinTable(
            name = "positions_optional_skills",
            joinColumns = @JoinColumn(name = "position_id"),
            inverseJoinColumns = @JoinColumn(name = "optional_skills_id")
    )
    private List<Skill> optionalSkills;

    private Integer minYearsExperience;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = true)
    private Location location;

    private int hoursPerWeek;

    @ElementCollection
    @Column(name = "responsibilities")
    private List<String> responsibilities;

    private String hiringProcess;

    @Column(columnDefinition = "TEXT")
    private String description;
}
