package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long>, CustomPositionRepository {

}