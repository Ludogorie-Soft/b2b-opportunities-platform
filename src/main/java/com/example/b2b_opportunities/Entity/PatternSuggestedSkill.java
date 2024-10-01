package com.example.b2b_opportunities.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pattern_suggested_skills")
@Getter
@Setter
public class PatternSuggestedSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pattern_id", nullable = false)
    private Pattern pattern;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
}