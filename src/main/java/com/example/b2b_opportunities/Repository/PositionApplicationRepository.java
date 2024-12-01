package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionApplicationRepository extends JpaRepository<PositionApplication, Long> {
    boolean existsByPositionIdAndTalentIdAndApplicationStatus(Long positionId, Long talentId, ApplicationStatus applicationStatus);

    @Query("SELECT pa FROM PositionApplication pa " +
            "JOIN pa.position p " +
            "JOIN p.project pr " +
            "WHERE pr.company.id = :companyId")
    List<PositionApplication> findAllApplicationsForCompany(@Param("companyId") Long companyId);
}
