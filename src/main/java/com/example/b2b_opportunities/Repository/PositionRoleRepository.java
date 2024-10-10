package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.PositionRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRoleRepository extends JpaRepository<PositionRole, Long> {
    Optional<PositionRole> findByName(String name);
}
