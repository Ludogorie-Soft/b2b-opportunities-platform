package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomProjectRepository {
    Page<Project> findCompanyProjectsByFilters(
            Long companyId,
            Long userCompanyId,
            boolean ownProjects,
            Pageable pageable);
}