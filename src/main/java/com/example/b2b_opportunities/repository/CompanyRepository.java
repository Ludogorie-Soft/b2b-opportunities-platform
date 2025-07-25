package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByIsApprovedFalse();

    Optional<Company> findByEmail(String email);

    Optional<Company> findByWebsite(String website);

    @Query("SELECT c FROM Company c WHERE c.linkedIn = :linkedIn")
    Optional<Company> findByLinkedIn(String linkedIn);

    Optional<Company> findByNameIgnoreCase(String name);

    Optional<Company> findByEmailConfirmationToken(String emailConfirmationToken);

    @Query(value = "SELECT * FROM companies c WHERE c.email_verification = 'ACCEPTED'", nativeQuery = true)
    List<Company> findCompaniesByEmailVerificationAccepted();

    @Query("SELECT c FROM Company c " +
            "JOIN c.filters f " +
            "WHERE LOWER(f.name) = 'default' " +
            "AND f.isEnabled = true " +
            "AND SIZE(c.skills) = 0 " +
            "GROUP BY c.id " +
            "HAVING COUNT(f) = 1")
    List<Company> findCompaniesWithSingleDefaultEnabledFilterAndNoCompanySkills();

    @Query(value = "SELECT COUNT(*) FROM companies WHERE :projectId = ANY (project_ids_notified)", nativeQuery = true)
    long countCompaniesThatWereNotifiedForProject(@Param("projectId") Long projectId);

}
