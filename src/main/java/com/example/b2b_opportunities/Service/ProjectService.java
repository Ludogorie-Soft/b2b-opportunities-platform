package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.ProjectEditRequestDto;
import com.example.b2b_opportunities.Dto.Request.ProjectRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.PermissionDeniedException;
import com.example.b2b_opportunities.Exception.common.DuplicateResourceException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.PositionMapper;
import com.example.b2b_opportunities.Mapper.ProjectMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Static.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final AdminService adminService;

    public ProjectResponseDto get(Long id) {
        return ProjectMapper.toDto(getProjectIfExists(id));
    }

    public List<ProjectResponseDto> getAll() {
        return ProjectMapper.toDtoList(projectRepository.findByProjectStatus(ProjectStatus.ACTIVE));
    }

    public ProjectResponseDto update(Long id, ProjectEditRequestDto dto) {
        Project project = getProjectIfExists(id);
        return createOrUpdate(dto, project);
    }

    public ProjectResponseDto create(ProjectRequestDto dto) {
        Project project = new Project();
        project.setDatePosted(LocalDateTime.now());
        project.setCompany(getCompanyIfExists(dto.getCompanyId()));

        return createOrUpdate(dto, project);
    }

    public void delete(Long id) {
        projectRepository.delete(getProjectIfExists(id));
    }

    public List<PositionResponseDto> getPositionsByProject(Long id) {
        Project project = getProjectIfExists(id);

        List<Position> positions = project.getPositions();
        if (project.getPositions() == null || project.getPositions().isEmpty())
            throw new NotFoundException("No positions found for Project with ID: " + id);

        return positions.stream().map(PositionMapper::toResponseDto).collect(Collectors.toList());
    }

    public ProjectResponseDto reactivateProject(Long projectId, Authentication authentication) {
        Project project = getProjectIfExists(projectId);
        User currentUser = adminService.getCurrentUserOrThrow(authentication);
        if (!Objects.equals(project.getCompany().getId(), currentUser.getCompany().getId())) {
            throw new PermissionDeniedException("Activation denied: project belongs to another company");
        }
        if (project.getProjectStatus().equals(ProjectStatus.ACTIVE)) {
            throw new DuplicateResourceException("This project is active already");
        }
        project.setProjectStatus(ProjectStatus.ACTIVE);
        // so that the project can be auto-deactivated in 3 weeks after reactivation
        project.setDatePosted(LocalDateTime.now());
        return ProjectMapper.toDto(projectRepository.save(project));
    }

    private ProjectResponseDto createOrUpdate(ProjectEditRequestDto dto, Project project) {
        project.setName(dto.getName());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setDuration(dto.getDuration());
        project.setDescription(dto.getDescription());
        project.setProjectStatus(ProjectStatus.ACTIVE);
        return ProjectMapper.toDto(projectRepository.save(project));
    }

    private Project getProjectIfExists(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Pattern with ID: " + id + " not found"));
    }

    private Company getCompanyIfExists(Long id) {
        return companyRepository.findById(id).orElseThrow(() -> new NotFoundException("Company with ID: " + id + " not found"));
    }
}
