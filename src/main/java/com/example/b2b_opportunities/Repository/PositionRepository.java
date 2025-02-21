package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Static.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    @Query("""
    SELECT DISTINCT pos 
    FROM Position pos
    JOIN FETCH pos.project proj
    LEFT JOIN FETCH proj.partnerGroupList pg
    LEFT JOIN FETCH pg.partners c
    LEFT JOIN FETCH pos.requiredSkills rs
    LEFT JOIN FETCH rs.skill s
    LEFT JOIN FETCH pos.workModes wm
    WHERE proj.projectStatus = :projectStatus
      AND (:rate IS NULL OR (pos.rate.min <= :rate AND pos.rate.max >= :rate))
      AND (:workModes IS NULL OR wm.id IN :workModes)
      AND (:skills IS NULL OR s.id IN :skills)
      AND (
           (:isPartnerOnly IS NULL AND (
                proj.isPartnerOnly = false 
                OR (proj.isPartnerOnly = true AND c.id = :companyId)
           ))
           OR
           (:isPartnerOnly = false AND proj.isPartnerOnly = false)
           OR
           (:isPartnerOnly = true AND proj.isPartnerOnly = true AND c.id = :companyId)
      )
""")
    Page<Position> findPositionsByFilters(
            @Param("isPartnerOnly") Boolean isPartnerOnly,
            @Param("companyId") Long companyId,
            @Param("projectStatus") ProjectStatus projectStatus,
            @Param("rate") Integer rate,
            @Param("workModes") Set<Long> workModes,
            @Param("skills") Set<Long> skills,
            Pageable pageable
    );

}