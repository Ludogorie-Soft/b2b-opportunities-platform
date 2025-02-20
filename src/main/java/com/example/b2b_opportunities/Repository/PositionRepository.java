package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Static.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByProjectIsPartnerOnlyFalseAndProjectProjectStatus(ProjectStatus projectStatus);

    @Query("SELECT DISTINCT pos FROM Position pos " +
            "JOIN pos.project proj " +
            "LEFT JOIN proj.partnerGroupList pg " +
            "LEFT JOIN pg.partners c " +
            "WHERE proj.isPartnerOnly = true " +
            "AND (c.id = :companyId OR proj.company.id = :companyId) " +
            "AND proj.projectStatus = :projectStatus " +
            "AND proj.company.isApproved = true")
    List<Position> findPartnerOnlyPositionsByCompanyInPartnerGroupsAndStatus(
            @Param("companyId") Long companyId,
            @Param("projectStatus") ProjectStatus projectStatus);

    @Query("SELECT p FROM Position p WHERE p.project.id IN :projectIds")
    List<Position> findByProjectIdsIn(Collection<Long> projectIds);
}