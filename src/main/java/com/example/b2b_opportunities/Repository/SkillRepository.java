package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Set<Skill> findAllByIdIn(List<Long> skillIDs);
}
