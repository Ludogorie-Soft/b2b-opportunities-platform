package com.example.b2b_opportunities.Service.Implementation;

import com.example.b2b_opportunities.Dto.Request.ProjectRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Mapper.PositionMapper;
import com.example.b2b_opportunities.Mapper.ProjectMapper;
import com.example.b2b_opportunities.Repository.PartnerGroupRepository;
import com.example.b2b_opportunities.Repository.PositionApplicationRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Service.Interface.ProjectService;
import com.example.b2b_opportunities.Service.Interface.UserService;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import com.example.b2b_opportunities.Static.ProjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final PartnerGroupRepository partnerGroupRepository;
    private final CompanyServiceImpl companyService;
    private final PositionApplicationRepository positionApplicationRepository;

    @Override
    public ProjectResponseDto get(Authentication authentication, Long id) {
        User user = userService.getCurrentUserOrThrow(authentication);
        log.info("User ID: {} attempting to access Project ID: {}", user.getId(), id);
        Company company = companyService.getUserCompanyOrThrow(user);
        Project project = getProjectIfExists(id);
        validateProjectIsAvailableToCompany(project, company);
        ProjectResponseDto responseDto = ProjectMapper.toDto(project);
        if (project.getPositions() != null) {
            responseDto.setPositionViews(getPositionViews(project));
            responseDto.setAcceptedApplications(getAcceptedApplications(project));
            responseDto.setTotalApplications(getTotalApplications(project));
        }
        return responseDto;
    }

    @Override
    public List<ProjectResponseDto> getAvailableProjects(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        log.info("User ID: {} attempting to access available projects", user.getId());
        Company company = companyService.getUserCompanyOrThrow(user);

        List<ProjectResponseDto> publicProjects = ProjectMapper.toDtoList(projectRepository
                .findByProjectStatusAndIsPartnerOnlyFalseAndCompanyIsApprovedTrue(ProjectStatus.ACTIVE));

        List<ProjectResponseDto> partnerProjects = getPartnerProjects(company);
        List<ProjectResponseDto> combinedProjects = new ArrayList<>();

        if (publicProjects != null && !publicProjects.isEmpty()) {
            combinedProjects.addAll(publicProjects);
        }
        if (partnerProjects != null && !partnerProjects.isEmpty()) {
            combinedProjects.addAll(partnerProjects);
        }

        return combinedProjects;
    }

    @Override
    public ProjectResponseDto update(Long id, ProjectRequestDto dto, Authentication authentication) {
        Project project = getProjectIfExists(id);
        User user = userService.getCurrentUserOrThrow(authentication);
        log.info("User ID: {} attempting to update Project ID: {}", user.getId(), project.getId());
        validateProjectBelongsToUser(user, project);
        return createOrUpdate(dto, project);
    }

    @Override
    public ProjectResponseDto create(Authentication authentication, ProjectRequestDto dto) {
        User user = userService.getCurrentUserOrThrow(authentication);
        log.info("User ID: {} attempting to create a project", user.getId());
        Company company = companyService.getUserCompanyOrThrow(user);
        Project project = new Project();
        project.setDatePosted(LocalDateTime.now());
        project.setCompany(company);
        return createOrUpdate(dto, project);
    }

    @Override
    public void delete(Long id, Authentication authentication) {
        Project project = getProjectIfExists(id);
        User user = userService.getCurrentUserOrThrow(authentication);
        log.info("User ID: {} attempting to delete Project ID: {}", user.getId(), project.getId());
        validateProjectBelongsToUser(user, project);
        projectRepository.delete(project);
    }

    @Override
    public List<PositionResponseDto> getPositionsByProject(Authentication authentication, Long id) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);

        Project project = getProjectIfExists(id);
        log.info("User ID: {} attempting to access positions of Project ID: {}", user.getId(), project.getId());
        validateProjectIsAvailableToCompany(project, userCompany);

        if (project.getPositions() == null || project.getPositions().isEmpty()) {
            return new ArrayList<>();
        }

        return PositionMapper.toResponseDtoList(project.getPositions());
    }

    @Override
    public ProjectResponseDto reactivateProject(Long projectId, Authentication authentication) {
        Project project = getProjectIfExists(projectId);
        User user = userService.getCurrentUserOrThrow(authentication);
        log.info("User ID: {} attempting to reactivate Project ID: {}", user.getId(), project.getId());
        validateProjectBelongsToUser(user, project);
        if (project.getProjectStatus().equals(ProjectStatus.ACTIVE)) {
            throw new InvalidRequestException("This project is active already", "projectStatus");
        }
        extendProjectDuration(project);
        return ProjectMapper.toDto(projectRepository.save(project));
    }

    @Override
    public void validateProjectIsAvailableToCompany(Project project, Company userCompany) {
        log.info("Validating if Project ID: {} belongs to Company ID: {}", project.getId(), userCompany.getId());
        if (project.getCompany().getId().equals(userCompany.getId())) {
            return;
        }
        if (project.isPartnerOnly()) {
            boolean isCompanyInGroup = project.getPartnerGroupList().stream()
                    .anyMatch(partnerGroup -> partnerGroup.getPartners().contains(userCompany));
            if (!isCompanyInGroup) {
                throw new PermissionDeniedException("This project is only shared with partners");
            }
            if (project.getProjectStatus().equals(ProjectStatus.INACTIVE)) {
                throw new PermissionDeniedException("This project is inactive");
            }
            if (!project.getCompany().isApproved()) {
                throw new NotFoundException("The company has not yet been approved to post public projects");
            }
        }
        if (!project.getCompany().isApproved()) {
            throw new PermissionDeniedException("Project posted by an unapproved company");
        }
    }

    private Long getPositionViews(Project project) {
        return project.getPositions().stream().mapToLong(Position::getViews).sum();
    }

    private Long getAcceptedApplications(Project project) {
        return project.getPositions().stream()
                .mapToLong(p -> positionApplicationRepository
                        .countByPositionIdAndApplicationStatus(p.getId(), ApplicationStatus.ACCEPTED))
                .sum();
    }

    private Long getTotalApplications(Project project) {
        return project.getPositions().stream()
                .mapToLong(p -> positionApplicationRepository
                        .countByPositionIdExcludingAwaitingCvOrTalent(p.getId())).sum();
    }

    private List<ProjectResponseDto> getPartnerProjects(Company company) {
        List<Project> partnerProjects = projectRepository.findPartnerOnlyProjectsByCompanyInPartnerGroupsAndStatus(company.getId(), ProjectStatus.ACTIVE);
        if (partnerProjects.isEmpty()) {
            return new ArrayList<>();
        }
        return ProjectMapper.toDtoList(partnerProjects);
    }

    private ProjectResponseDto createOrUpdate(ProjectRequestDto dto, Project project) {
        project.setName(dto.getName());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setDuration(dto.getDuration());
        project.setDescription(dto.getDescription());
        project.setDateUpdated(LocalDateTime.now());
        extendProjectDuration(project);
        if (dto.isPartnerOnly()) {
            project.setPartnerOnly(true);
            List<PartnerGroup> projectPartnerGroups = getPartnerGroupsOrThrow(dto.getPartnerGroups());
            validatePartnerGroupsBelongToCompany(project.getCompany(), projectPartnerGroups);
            project.setPartnerGroupList(projectPartnerGroups);
        } else {
            project.setPartnerOnly(false);
            project.setPartnerGroupList(null);
        }
        return ProjectMapper.toDto(projectRepository.save(project));
    }

    private List<PartnerGroup> getPartnerGroupsOrThrow(List<Long> partnerGroupIds) {
        List<PartnerGroup> partnerGroups = partnerGroupRepository.findAllById(partnerGroupIds);

        // Check for missing PartnerGroup IDs
        List<Long> missingPartnerGroupIds = partnerGroupIds.stream()
                .filter(id -> partnerGroups.stream().noneMatch(partnerGroup -> partnerGroup.getId().equals(id)))
                .toList();

        if (!missingPartnerGroupIds.isEmpty()) {
            throw new NotFoundException("PartnerGroups with ID(s) " + missingPartnerGroupIds + " not found.");
        }

        return partnerGroups;
    }

    private void validatePartnerGroupsBelongToCompany(Company company, List<PartnerGroup> partnerGroups) {
        Set<PartnerGroup> companyPartnerGroup = company.getPartnerGroups();
        for (PartnerGroup partnerGroup : partnerGroups) {
            if (!companyPartnerGroup.contains(partnerGroup)) {
                throw new PermissionDeniedException("Partner group with ID " + partnerGroup.getId() + " does not belong to this company.");
            }
        }
    }

    private Project getProjectIfExists(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Project with ID: " + id + " not found"));
    }


    private void validateProjectBelongsToUser(User user, Project project) {
        if (!Objects.equals(user.getCompany().getId(), project.getCompany().getId())) {
            throw new PermissionDeniedException("Project belongs to another company");
        }
    }

    private void extendProjectDuration(Project project) {
        project.setExpiryDate(LocalDateTime.now().plusWeeks(3));
        project.setProjectStatus(ProjectStatus.ACTIVE);
    }
}
