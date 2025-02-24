package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Talent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalentRepository extends JpaRepository<Talent, Long> {
    List<Talent> findByCompanyId(Long companyId);

    @Query("SELECT t FROM Talent t " +
            "JOIN t.company c " +
            "LEFT JOIN c.partnerGroups pg " +
            "LEFT JOIN pg.partners p " +
            "WHERE t.isActive = true AND " +
            "(c.talentsSharedPublicly = true OR (p.id = :companyId OR c.id = :companyId))")
    Page<Talent> findAllActiveTalentsVisibleToCompany(@Param("companyId") Long companyId, Pageable pageable);

}
