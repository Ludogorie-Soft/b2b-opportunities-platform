package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.dto.requestDtos.ProjectRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.PositionResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.ProjectResponseDto;
import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.PartnerGroup;
import com.example.b2b_opportunities.entity.Position;
import com.example.b2b_opportunities.entity.Project;
import com.example.b2b_opportunities.entity.User;
import com.example.b2b_opportunities.exception.common.InvalidRequestException;
import com.example.b2b_opportunities.exception.common.NotFoundException;
import com.example.b2b_opportunities.exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.mapper.PositionMapper;
import com.example.b2b_opportunities.mapper.ProjectMapper;
import com.example.b2b_opportunities.repository.PartnerGroupRepository;
import com.example.b2b_opportunities.repository.PositionApplicationRepository;
import com.example.b2b_opportunities.repository.PositionRepository;
import com.example.b2b_opportunities.repository.ProjectRepository;
import com.example.b2b_opportunities.services.interfaces.ProjectService;
import com.example.b2b_opportunities.services.interfaces.UserService;
import com.example.b2b_opportunities.enums.ApplicationStatus;
import com.example.b2b_opportunities.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
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
    private final PositionRepository positionRepository;

    @Override
    public ProjectResponseDto get(Authentication authentication, Long id) {
        User user = userService.getCurrentUserOrThrow(authentication);
        log.info("User ID: {} attempting to access Project ID: {}", user.getId(), id);
        Company company = companyService.getUserCompanyOrThrow(user);
        Project project = getProjectIfExists(id);
        if (!isAdmin(authentication)) {
            validateProjectIsAvailableToCompany(project, company);
        }
        ProjectResponseDto responseDto = ProjectMapper.toDto(project);
        if (project.getPositions() != null) {
            responseDto.setPositionViews(getPositionViews(project));
            responseDto.setAcceptedApplications(getAcceptedApplications(project));
            responseDto.setTotalApplications(getTotalApplications(project));
        }
        return responseDto;
    }

    @Override
    public Page<ProjectResponseDto> getAvailableProjects(Authentication authentication,
                                                         int offset,
                                                         int pageSize,
                                                         String sort,
                                                         boolean ascending) {
        User user = userService.getCurrentUserOrThrow(authentication);
        log.info("User ID: {} attempting to access available projects", user.getId());

        Company company = companyService.getUserCompanyOrThrow(user);


        if (pageSize <= 0) {
            pageSize = 10;
        }

        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sort));

        Page<Project> publicProjectsPage = projectRepository
                .findByProjectStatusAndIsPartnerOnlyFalseAndCompanyIsApprovedTrue(ProjectStatus.ACTIVE, pageable);

        Page<ProjectResponseDto> publicProjectsDtoPage = publicProjectsPage.map(ProjectMapper::toDto);

        List<ProjectResponseDto> partnerProjects = getPartnerProjects(company);

        List<ProjectResponseDto> combinedProjects = new ArrayList<>(publicProjectsDtoPage.getContent());
        if (partnerProjects != null && !partnerProjects.isEmpty()) {
            combinedProjects.addAll(partnerProjects);
        }

        return new PageImpl<>(combinedProjects, pageable, publicProjectsDtoPage.getTotalElements());
    }

    @Override
    public ProjectResponseDto update(Long id, ProjectRequestDto dto, Authentication authentication) {
        Project project = getProjectIfExists(id);
        User user = userService.getCurrentUserOrThrow(authentication);
        log.info("User ID: {} attempting to update Project ID: {}", user.getId(), project.getId());
        validateProjectBelongsToUser(user, project);
        if(positionRepository.existsOpenedPositionByProjectId(id)) {
            project.setProjectStatus(ProjectStatus.ACTIVE);
        } else {
            project.setProjectStatus(ProjectStatus.INACTIVE);
        }
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
        project.setProjectStatus(ProjectStatus.INACTIVE);
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
        project.setExpiryDate(LocalDateTime.now().plusWeeks(3));
        project.setProjectStatus(ProjectStatus.ACTIVE);
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

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void markProjectsForReactivation() {
        Set<Project> projectsToBeReactivated = new HashSet<>();
        projectsToBeReactivated.addAll(projectRepository.findProjectsExpiringInTwoDaysAndNotMarked());
        projectsToBeReactivated.addAll(projectRepository.findExpiredAndActiveProjectsNotMarked());

        projectsToBeReactivated.forEach(project -> project.setCanReactivate(true));
        projectRepository.saveAll(projectsToBeReactivated);
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
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
        project.setCanReactivate(false);
        project.setExpiryDate(LocalDateTime.now().plusWeeks(3));
        if (dto.isPartnerOnly()) {
            project.setPartnerOnly(true);
            List<PartnerGroup> projectPartnerGroups = getPartnerGroupsOrThrow(dto.getPartnerGroups());
            validatePartnerGroupsBelongToCompany(project.getCompany(), projectPartnerGroups);
            project.setPartnerGroupList(new HashSet<>(projectPartnerGroups));
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
}
