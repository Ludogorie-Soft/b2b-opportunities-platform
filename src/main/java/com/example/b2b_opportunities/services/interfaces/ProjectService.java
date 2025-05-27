package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.dto.requestDtos.ProjectRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.PositionResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.ProjectResponseDto;
import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProjectService {
    ProjectResponseDto get(Authentication authentication, Long id);

    Page<ProjectResponseDto> getAvailableProjects(Authentication authentication, int offset, int pageSize, String sort, boolean ascending);

    ProjectResponseDto update(Long id, ProjectRequestDto dto, Authentication authentication);

    ProjectResponseDto create(Authentication authentication, ProjectRequestDto dto);

    void delete(Long id, Authentication authentication);

    List<PositionResponseDto> getPositionsByProject(Authentication authentication, Long id);

    ProjectResponseDto reactivateProject(Long projectId, Authentication authentication);

    void validateProjectIsAvailableToCompany(Project project, Company userCompany);

    void markProjectsForReactivation();
}
