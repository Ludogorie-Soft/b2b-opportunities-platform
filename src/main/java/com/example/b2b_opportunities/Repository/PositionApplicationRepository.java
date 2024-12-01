package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionApplicationRepository extends JpaRepository<PositionApplication, Long> {
    boolean existsByPositionIdAndTalentIdAndApplicationStatus(Long positionId, Long talentId, ApplicationStatus applicationStatus);
}
