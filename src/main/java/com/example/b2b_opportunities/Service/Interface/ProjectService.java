package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Dto.Request.ProjectRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Project;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProjectService {
    ProjectResponseDto get(Authentication authentication, Long id);

    List<ProjectResponseDto> getAvailableProjects(Authentication authentication);

    ProjectResponseDto update(Long id, ProjectRequestDto dto, Authentication authentication);

    ProjectResponseDto create(Authentication authentication, ProjectRequestDto dto);

    void delete(Long id, Authentication authentication);

    List<PositionResponseDto> getPositionsByProject(Authentication authentication, Long id);

    ProjectResponseDto reactivateProject(Long projectId, Authentication authentication);

    void validateProjectIsAvailableToCompany(Project project, Company userCompany);
}
