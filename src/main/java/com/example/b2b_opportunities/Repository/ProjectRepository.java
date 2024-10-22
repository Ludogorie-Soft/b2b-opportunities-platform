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
  
    @Query(value = "SELECT * FROM projects p WHERE p.date_posted = CURRENT_DATE - INTERVAL '19 days'", nativeQuery = true)
    List<Project> findProjectsExpiringInTwoDays();

    @Query(value = "SELECT * FROM projects p WHERE p.date_posted <= CURRENT_DATE - INTERVAL :daysToSubtract DAY", nativeQuery = true)
    List<Project> findProjectsOlderThan(@Param("daysToSubtract") int daysToSubtract);

    List<Project> findByProjectStatusAndIsPartnerOnlyFalse(ProjectStatus projectStatus);
}