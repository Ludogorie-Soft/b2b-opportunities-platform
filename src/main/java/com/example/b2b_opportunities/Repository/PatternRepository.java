package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatternRepository extends JpaRepository<Pattern, Long> {
    Optional<Pattern> findByName(String name);
}
