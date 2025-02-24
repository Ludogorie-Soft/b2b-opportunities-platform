package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Static.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    @Query("""
                SELECT pos 
                FROM Position pos
                JOIN FETCH pos.project proj
                LEFT JOIN FETCH proj.partnerGroupList pg
                LEFT JOIN FETCH pg.partners c
                WHERE 
                    (:isPartnerOnly = false AND proj.isPartnerOnly = false)
                    OR (
                        :isPartnerOnly = true 
                        AND proj.isPartnerOnly = true 
                        AND (c.id = :companyId OR proj.company.id = :companyId) 
                        AND proj.company.isApproved = true
                    )
                AND proj.projectStatus = :projectStatus
            """)
    Page<Position> findPositionsByIsPartnerOnlyAndStatus(
            @Param("isPartnerOnly") boolean isPartnerOnly,
            @Param("companyId") Long companyId,
            @Param("projectStatus") ProjectStatus projectStatus,
            Pageable pageable);

    @Query("SELECT p FROM Position p WHERE p.project.id IN :projectIds")
    List<Position> findByProjectIdsIn(Collection<Long> projectIds);
}