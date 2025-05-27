package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.TalentExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TalentExperienceRepository extends JpaRepository<TalentExperience, Long> {
    void deleteAllByTalentId(Long talentId);
}
