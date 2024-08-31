package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
    Optional<Employer> findByEmail(String email);

    Optional<Employer> findByUsername(String username);
}
