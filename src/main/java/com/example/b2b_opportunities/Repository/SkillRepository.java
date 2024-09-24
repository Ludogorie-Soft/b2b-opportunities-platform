package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByIdentifier(String identifier);
}
