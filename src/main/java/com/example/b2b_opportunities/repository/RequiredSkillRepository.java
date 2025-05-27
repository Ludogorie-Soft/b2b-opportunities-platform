package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Position;
import com.example.b2b_opportunities.entity.RequiredSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequiredSkillRepository extends JpaRepository<RequiredSkill, Long> {
    List<RequiredSkill> findByPosition(Position position);
}
