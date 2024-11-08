package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Static.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByDateUpdatedAfter(LocalDateTime dateTime);

    @Query(value = "SELECT * FROM projects p WHERE p.expiry_date = CURRENT_DATE + INTERVAL '2 days'", nativeQuery = true)
    List<Project> findProjectsExpiringInTwoDays();

    @Query(value = "SELECT * FROM projects p WHERE p.expiry_date <= CURRENT_DATE AND p.project_status = 'ACTIVE'", nativeQuery = true)
    List<Project> findExpiredAndActiveProjects();

    List<Project> findByProjectStatusAndIsPartnerOnlyFalse(ProjectStatus projectStatus);

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN p.partnerGroupList pg " +
            "LEFT JOIN pg.partners c " +
            "WHERE p.isPartnerOnly = true " +
            "AND (c.id = :companyId OR p.company.id = :companyId) " +
            "AND p.projectStatus = :projectStatus")
    List<Project> findPartnerOnlyProjectsByCompanyInPartnerGroupsAndStatus(@Param("companyId") Long companyId,
                                                                           @Param("projectStatus") ProjectStatus projectStatus);
}