package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Static.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface CustomPositionRepository {
    Page<Position> findPositionsByFilters(
            Boolean isPartnerOnly,
            Long companyId,
            ProjectStatus projectStatus,
            Integer rate,
            Set<Long> workModes,
            Set<Long> skills,
            Long userCompanyId,
            Pageable pageable);
}
