package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.ProjectRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Mapper.PositionMapper;
import com.example.b2b_opportunities.Mapper.ProjectMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.PartnerGroupRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Static.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final UserService userService;
    private final MailService mailService;
    private final PartnerGroupRepository partnerGroupRepository;

    public ProjectResponseDto get(Authentication authentication, Long id) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getCompanyIfExists(user.getCompany().getId());
        Project project = getProjectIfExists(id);
        validateProjectIsAvailableToCompany(project, company);
        //TODO - also check if its posted by approved company
        return ProjectMapper.toDto(project);
    }

    public List<ProjectResponseDto> getAvailableProjects(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getCompanyIfExists(user.getCompany().getId());

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

    public ProjectResponseDto update(Long id, ProjectRequestDto dto, Authentication authentication) {
        Project project = getProjectIfExists(id);
        validateProjectBelongsToUser(authentication, project);
        return createOrUpdate(dto, project);
    }

    public ProjectResponseDto create(Authentication authentication, ProjectRequestDto dto) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getCompanyIfExists(user.getCompany().getId());
        Project project = new Project();
        project.setDatePosted(LocalDateTime.now());
        project.setExpiryDate(LocalDateTime.now().plusDays(21));
        project.setCompany(company);
        return createOrUpdate(dto, project);
    }

    public void delete(Long id, Authentication authentication) {
        Project project = getProjectIfExists(id);
        validateProjectBelongsToUser(authentication, project);
        projectRepository.delete(project);
    }

    public List<PositionResponseDto> getPositionsByProject(Authentication authentication, Long id) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = getCompanyIfExists(user.getCompany().getId());

        Project project = getProjectIfExists(id);
        validateProjectIsAvailableToCompany(project, userCompany);

        if (project.getPositions() == null || project.getPositions().isEmpty()) {
            return new ArrayList<>();
        }

        return PositionMapper.toResponseDtoList(project.getPositions());
    }

    public ProjectResponseDto reactivateProject(Long projectId, Authentication authentication) {
        Project project = getProjectIfExists(projectId);
        validateProjectBelongsToUser(authentication, project);
        if (project.getProjectStatus().equals(ProjectStatus.ACTIVE)) {
            throw new AlreadyExistsException("This project is active already");
        }
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setExpiryDate(LocalDateTime.now().plusDays(21));
        return ProjectMapper.toDto(projectRepository.save(project));
    }

    //Once per day at 13:00
    @Scheduled(cron = "0 0 13 * * *")
    public void processExpiringProjects() {
        List<Project> expiringProjects = projectRepository.findProjectsExpiringInTwoDays();
        for (Project project : expiringProjects) {
            mailService.sendProjectExpiringMail(project);
        }
        List<Project> expiredProjects = projectRepository.findExpiredAndActiveProjects();
        for (Project project : expiredProjects) {
            project.setProjectStatus(ProjectStatus.INACTIVE);
            projectRepository.save(project);
        }
    }

    public void validateProjectIsAvailableToCompany(Project project, Company userCompany) {
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
        project.setProjectStatus(ProjectStatus.ACTIVE);
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

    private Company getCompanyIfExists(Long id) {
        if (id == null) {
            throw new NotFoundException("User does not associate with any company");
        }
        return companyRepository.findById(id).orElseThrow(() -> new NotFoundException("Company with ID: " + id + " not found"));
    }

    private void validateProjectBelongsToUser(Authentication authentication, Project project) {
        User user = userService.getCurrentUserOrThrow(authentication);
        if (!Objects.equals(user.getCompany().getId(), project.getCompany().getId())) {
            throw new PermissionDeniedException("Project belongs to another company");
        }
    }
}
