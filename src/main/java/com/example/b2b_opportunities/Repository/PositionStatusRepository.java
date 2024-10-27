package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.PositionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionStatusRepository extends JpaRepository<PositionStatus, Long> {
}
