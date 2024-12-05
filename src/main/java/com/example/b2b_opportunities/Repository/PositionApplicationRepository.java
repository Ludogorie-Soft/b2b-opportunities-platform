package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionApplicationRepository extends JpaRepository<PositionApplication, Long> {
    Optional<PositionApplication> findFirstByPositionIdAndTalentIdAndApplicationStatusIn(Long positionId, Long talentId, List<ApplicationStatus> applicationStatuses);

    //    the method below checks if 1 Company has already applied to 1 position
//    boolean existsByPositionIdAndTalent_CompanyIdAndApplicationStatusIn(Long positionId, Long companyId, List<ApplicationStatus> applicationStatuses);
    @Query("SELECT pa FROM PositionApplication pa " +
            "JOIN pa.position p " +
            "JOIN p.project pr " +
            "WHERE pr.company.id = :companyId")
    List<PositionApplication> findAllApplicationsForCompany(@Param("companyId") Long companyId);

    @Query("SELECT pa FROM PositionApplication pa " +
            "JOIN pa.talent t " +
            "WHERE t.company.id = :companyId")
    List<PositionApplication> findAllMyApplications(@Param("companyId") Long companyId);
}
