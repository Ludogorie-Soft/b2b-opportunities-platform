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
            "LEFT JOIN t.workModes wm " +
            "LEFT JOIN t.talentExperience te " +
            "LEFT JOIN te.skillExperienceList se " +
            "LEFT JOIN se.skill s " +
            "WHERE t.isActive = true AND " +
            "t.company.id <> :companyId " +
            "AND (:workModesIds IS NULL OR wm.id IN :workModesIds) " +
            "AND (:skillsIds IS NULL OR s.id IN :skillsIds) " +
            "AND (:rate IS NULL OR (:rate > 0 AND (t.maxRate IS NULL OR :rate <= t.maxRate)))")
    Page<Talent> findAllActiveTalentsExcludingCompany(
            @Param("companyId") Long companyId,
            @Param("workModesIds") List<Long> workModesIds,
            @Param("skillsIds") List<Long> skillsIds,
            @Param("rate") Integer rate,
            Pageable pageable);
}
