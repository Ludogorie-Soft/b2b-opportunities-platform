package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalentRepository extends JpaRepository<Talent, Long> {
    List<Talent> findByCompanyId(Long companyId);

    @Query("SELECT t FROM Talent t WHERE t.company.talentsSharedPublicly = true AND t.isActive = true")
    List<Talent> findAllActivePublicTalents();

    @Query("SELECT t FROM Talent t " +
            "JOIN t.company c " +
            "JOIN c.partnerGroups pg " +
            "JOIN pg.partners p " +
            "WHERE (p.id = :companyId OR c.id = :companyId) AND t.isActive = true")
    List<Talent> findActiveTalentsSharedWithUserCompany(@Param("companyId") Long companyId);
}
