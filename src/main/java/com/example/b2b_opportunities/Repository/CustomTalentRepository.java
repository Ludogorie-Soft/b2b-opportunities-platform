package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Talent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface  CustomTalentRepository {
    Page<Talent> findTalentsByFilters(
            Long currentCompanyId,
            Set<Long> workModes,
            Set<Long> skills,
            Integer rate,
            Pageable pageable,
            boolean onlyMyCompany);
}
