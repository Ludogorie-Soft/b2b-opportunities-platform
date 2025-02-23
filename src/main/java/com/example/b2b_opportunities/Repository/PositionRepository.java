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
public interface PositionRepository extends JpaRepository<Position, Long>, CustomPositionRepository {

}