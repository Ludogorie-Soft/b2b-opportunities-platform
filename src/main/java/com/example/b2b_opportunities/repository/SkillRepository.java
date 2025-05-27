package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Set<Skill> findAllByIdIn(List<Long> skillIDs);

    @Query("SELECT s FROM Skill s WHERE s.id <> 1 AND (s.parent.id IS NULL OR s.parent.id <> 1)")
    List<Skill> findAllTechSkills();
}
