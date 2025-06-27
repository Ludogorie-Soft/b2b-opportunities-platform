package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.PositionApplication;
import com.example.b2b_opportunities.entity.Talent;
import com.example.b2b_opportunities.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
            "WHERE pr.company.id = :companyId " +
            "AND pa.applicationStatus NOT IN :excludedStatuses")
    List<PositionApplication> findAllApplicationsForMyPositions(@Param("companyId") Long companyId,
                                                                @Param("excludedStatuses") List<ApplicationStatus> excludedStatuses);


    @Query("SELECT pa FROM PositionApplication pa " +
            "WHERE pa.talentCompany.id = :companyId " +
            "AND pa.applicationStatus <> :excludedStatus")
    List<PositionApplication> findAllMyApplications(@Param("companyId") Long companyId,
                                                    @Param("excludedStatus") ApplicationStatus excludedStatus);

    Optional<PositionApplication> findByPositionIdAndTalentId(Long positionId, Long talentId);

    @Query("SELECT pa FROM PositionApplication pa WHERE pa.position.id = :positionId AND pa.applicationStatus != 'AWAITING_CV_OR_TALENT'")
    List<PositionApplication> findByPositionIdExcludingAwaitingCvOrTalent(@Param("positionId") Long positionId);
    List<PositionApplication> findByPositionIdAndApplicationStatus(Long positionId, ApplicationStatus applicationStatus);

    Long countByPositionIdAndApplicationStatus(Long positionId, ApplicationStatus status);

    @Query("SELECT COUNT(pa) FROM PositionApplication pa WHERE pa.position.id = :positionId AND pa.applicationStatus != 'AWAITING_CV_OR_TALENT'")
    Long countByPositionIdExcludingAwaitingCvOrTalent(@Param("positionId") Long positionId);

    @Query("SELECT p FROM PositionApplication p WHERE p.applicationDateTime >= :start AND p.applicationDateTime < :end AND p.applicationStatus = 'IN_PROGRESS'")
    List<PositionApplication> findAllApplicationsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(pa) > 0 FROM PositionApplication pa WHERE pa.talent = :talent AND pa.position.project.company = :company")
    boolean existsByTalentAndCompany(@Param("talent") Talent talent, @Param("company") Company company);

    long countByTalentId(Long talentId);

    @Query("SELECT COUNT(pa) FROM PositionApplication pa WHERE pa.position.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT MAX(pa.applicationDateTime) FROM PositionApplication pa WHERE pa.position.project.id = :projectId")
    LocalDateTime findLastApplicationDateTimeByProjectId(@Param("projectId") Long projectId);
}
