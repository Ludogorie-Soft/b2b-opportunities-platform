package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Project;
import com.example.b2b_opportunities.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.time.LocalDateTime;

public interface ProjectRepository extends JpaRepository<Project, Long>, CustomProjectRepository {
    @Query("SELECT p FROM Project p WHERE p.projectStatus = :status AND p.isPartnerOnly = false AND p.company.id = :companyId")
    List<Project> findActiveNonPartnerOnlyProjectsByCompanyId(
            @Param("status") ProjectStatus status,
            @Param("companyId") Long companyId
    );

    @Query("""
                SELECT p FROM Project p
                WHERE p.projectStatus = :status 
                AND p.isPartnerOnly = true 
                AND p.company.id = :projectOwnerCompanyId 
                AND EXISTS (
                      SELECT pg FROM PartnerGroup pg 
                      JOIN pg.partners partnerCompany
                      WHERE pg.company.id = :projectOwnerCompanyId
                      AND partnerCompany.id = :requestingCompanyId
                )
            """)
    List<Project> findActivePartnerOnlyProjectsSharedWithCompany(
            @Param("status") ProjectStatus status,
            @Param("projectOwnerCompanyId") Long projectOwnerCompanyId,
            @Param("requestingCompanyId") Long requestingCompanyId
    );

    List<Project> findAllByDateUpdatedAfter(LocalDateTime dateTime);

    @Query(value = "SELECT * FROM projects p WHERE DATE(p.expiry_date) = CURRENT_DATE + INTERVAL '2 days'", nativeQuery = true)
    List<Project> findProjectsExpiringInTwoDays();

    @Query(value = "SELECT * FROM projects p WHERE DATE(p.expiry_date) <= CURRENT_DATE AND p.project_status = 'ACTIVE'", nativeQuery = true)
    List<Project> findExpiredAndActiveProjects();

    @Query(value = "SELECT * FROM projects p WHERE DATE(p.expiry_date) = CURRENT_DATE + INTERVAL '2 days' AND p.can_reactivate = false", nativeQuery = true)
    List<Project> findProjectsExpiringInTwoDaysAndNotMarked();

    @Query(value = "SELECT * FROM projects p WHERE DATE(p.expiry_date) <= CURRENT_DATE AND p.project_status = 'ACTIVE' AND p.can_reactivate = false", nativeQuery = true)
    List<Project> findExpiredAndActiveProjectsNotMarked();

    Page<Project> findByProjectStatusAndIsPartnerOnlyFalseAndCompanyIsApprovedTrue(ProjectStatus projectStatus, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN p.partnerGroupList pg " +
            "LEFT JOIN pg.partners c " +
            "WHERE p.isPartnerOnly = true " +
            "AND (c.id = :companyId OR p.company.id = :companyId) " +
            "AND p.projectStatus = :projectStatus " +
            "AND p.company.isApproved = true")
    List<Project> findPartnerOnlyProjectsByCompanyInPartnerGroupsAndStatus(@Param("companyId") Long companyId,
                                                                           @Param("projectStatus") ProjectStatus projectStatus);

    @Query("select p from Project p where p.company.id = :companyId")
    Page<Project> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.projectStatus = :status")
    Page<Project> findProjectsByStatus(@Param("status") ProjectStatus status, Pageable pageable);

}