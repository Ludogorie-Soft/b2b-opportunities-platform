package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long>, CustomPositionRepository {
    @Query("""
    SELECT COUNT(p) > 0 FROM Position p WHERE p.project.id = :projectId AND p.status.name = 'Opened'
    """)
    boolean existsOpenedPositionByProjectId(@Param("projectId") Long projectId);
}