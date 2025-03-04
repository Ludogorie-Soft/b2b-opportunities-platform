package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Static.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.time.LocalDateTime;

public interface ProjectRepository extends JpaRepository<Project, Long> {
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
}