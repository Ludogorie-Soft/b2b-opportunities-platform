package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByEmail(String email);

    Optional<Company> findByWebsite(String website);

    @Query("SELECT c FROM Company c WHERE c.linkedIn = :linkedIn")
    Optional<Company> findByLinkedIn(String linkedIn);

    Optional<Company> findByNameIgnoreCase(String name);

    Optional<Company> findByEmailConfirmationToken(String emailConfirmationToken);

    @Query(value = "SELECT * FROM companies c WHERE c.email_verification = 'ACCEPTED'", nativeQuery = true)
    List<Company> findCompaniesByEmailVerificationAccepted();
}
