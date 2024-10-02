package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.CompanyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyTypeRepository extends JpaRepository<CompanyType, Long> {
    Optional<CompanyType> findByName(String name);
}
