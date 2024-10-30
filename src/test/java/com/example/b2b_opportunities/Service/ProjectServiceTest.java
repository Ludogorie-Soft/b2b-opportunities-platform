package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.ProjectRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Mapper.PositionMapper;
import com.example.b2b_opportunities.Mapper.ProjectMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.PartnerGroupRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Static.ProjectStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ProjectServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private MailService mailService;
    @Mock
    private CompanyService companyService;

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PartnerGroupRepository partnerGroupRepository;

    @Mock
    private Authentication authentication;

    private User user;
    private Company company;
    private Project project;
    private ProjectResponseDto projectResponseDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize sample objects
        user = new User();

        company = new Company();
        company.setId(1L);
        company.setUsers(List.of(user));

        user.setCompany(company);

        project = new Project();
        project.setId(1L);
        project.setCompany(company);
        project.setProjectStatus(ProjectStatus.ACTIVE);

        projectResponseDto = new ProjectResponseDto();
        projectResponseDto.setId(project.getId());
    }

    @Test
    public void WhenValidRequestShouldReturnsProjectResponseDto() {
        // Arrange
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(user.getCompany().getId())).thenReturn(Optional.of(company));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            mockedMapper.when(() -> ProjectMapper.toDto(project)).thenReturn(projectResponseDto);

            ProjectResponseDto result = projectService.get(authentication, 1L);

            Assertions.assertNotNull(result, "Result should not be null");
            Assertions.assertEquals(projectResponseDto, result, "DTOs should match");
        }
    }
    @Test
    public void WhenUserNotFoundThrowsException() {
        when(userService.getCurrentUserOrThrow(authentication))
                .thenThrow(new AuthenticationFailedException("User not authenticated"));

        assertThrows(AuthenticationFailedException.class, () -> projectService.get(authentication, 1L));
    }

    @Test
    public void shouldThrowAnExceptionWhenProjectNotFound() {
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(user.getCompany().getId())).thenReturn(Optional.of(company));
        when(projectRepository.findById(1L)).thenThrow(new NotFoundException("Project with ID: " + 1L + " not found"));

        assertThrows(NotFoundException.class, () -> projectService.get(authentication, 1L));
    }

    @Test
    public void shouldThrowAnExceptionWhenProjectNotAvailableToCompany() {
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(user.getCompany().getId())).thenReturn(Optional.of(company));

        project.setPartnerOnly(true);

        PartnerGroup partnerGroup = new PartnerGroup();
        Company partnerCompany = new Company();
        partnerCompany.setId(3L);
        partnerGroup.setPartners(Collections.singleton(partnerCompany));

        project.setPartnerGroupList(Collections.singletonList(partnerGroup));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(PermissionDeniedException.class, () -> projectService.get(authentication, 1L));
    }
    @Test
    public void shouldGetAvailableProjectsWhenUserHasCompany() {
        Project partnerProject = new Project();
        partnerProject.setId(123123123L);
        partnerProject.setCompany(company);
        partnerProject.setProjectStatus(ProjectStatus.ACTIVE);

        Project publicProject = new Project();
        publicProject.setId(1L);
        publicProject.setCompany(company);
        publicProject.setProjectStatus(ProjectStatus.ACTIVE);
        publicProject.setName("Public Project");
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        when(projectRepository.findByProjectStatusAndIsPartnerOnlyFalse(ProjectStatus.ACTIVE))
                .thenReturn(Collections.singletonList(publicProject));
        when(projectRepository.findPartnerOnlyProjectsByCompanyInPartnerGroupsAndStatus(company.getId(), ProjectStatus.ACTIVE))
                .thenReturn(Collections.singletonList(partnerProject));

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            ProjectResponseDto publicProjectDto = ProjectResponseDto.builder()
                    .id(publicProject.getId())
                    .companyId(publicProject.getCompany().getId())
                    .datePosted(LocalDateTime.now())
                    .name(publicProject.getName())
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(30))
                    .duration(1)
                    .Description("Public project description")
                    .status("ACTIVE")
                    .isPartnerOnly(false)
                    .build();

            ProjectResponseDto partnerProjectDto = ProjectResponseDto.builder()
                    .id(partnerProject.getId())
                    .companyId(partnerProject.getCompany().getId())
                    .datePosted(LocalDateTime.now())
                    .name("Partner Project")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(60))
                    .duration(2)
                    .Description("Partner project description")
                    .status("ACTIVE")
                    .isPartnerOnly(true)
                    .build();

            mockedMapper.when(() -> ProjectMapper.toDtoList(anyList()))
                    .thenReturn(Arrays.asList(publicProjectDto, partnerProjectDto));

            List<ProjectResponseDto> result = projectService.getAvailableProjects(authentication);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(publicProjectDto, result.get(0));
            Assertions.assertEquals(partnerProjectDto, result.get(1));
        }
    }

    @Test
    public void shouldReturnUpdatedProjectResponseDtoWhenValidUpdateRequest() {
        Long projectId = 1L;
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("Updated Project");
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(30));
        dto.setDuration(2);
        dto.setDescription("Updated description");
        dto.setPartnerOnly(false);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            mockedMapper.when(() -> ProjectMapper.toDto(any(Project.class))).thenReturn(projectResponseDto);

            ProjectResponseDto result = projectService.update(projectId, dto, authentication);

            Assertions.assertNotNull(result, "Result should not be null");
            Assertions.assertEquals(projectResponseDto, result, "DTOs should match");
        }
    }

    @Test
    public void shouldThrowPermissionDeniedExceptionWhenProjectBelongsToDifferentCompany() {
        Long projectId = 1L;
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("Unauthorized Project Update");

        Company otherCompany = new Company();
        otherCompany.setId(999L);
        project.setCompany(otherCompany);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(PermissionDeniedException.class, () -> projectService.update(projectId, dto, authentication));
    }

    @Test
    public void updateShouldIncludePartnerGroupsWhenPartnerOnlyProject() {
        Long projectId = 1L;
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("Partner Project Update");
        dto.setPartnerOnly(true);
        dto.setPartnerGroupIds(List.of(1L));

        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(1L);
        Set<PartnerGroup> partnerGroups = new HashSet<>();
        partnerGroups.add(partnerGroup);

        company.setPartnerGroups(partnerGroups);
        project.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(partnerGroupRepository.findAllById(dto.getPartnerGroupIds())).thenReturn(List.of(partnerGroup));

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            mockedMapper.when(() -> ProjectMapper.toDto(any(Project.class))).thenReturn(projectResponseDto);

            ProjectResponseDto result = projectService.update(projectId, dto, authentication);

            Assertions.assertNotNull(result, "Result should not be null");
            Assertions.assertEquals(projectResponseDto, result, "DTOs should match");
        }
    }

    @Test
    public void shouldReturnProjectResponseDtoWhenValidCreateRequest() {
        // Arrange
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("New Project");
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(30));
        dto.setDuration(1);
        dto.setDescription("New project description");
        dto.setPartnerOnly(false);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(user.getCompany().getId())).thenReturn(Optional.of(company));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            mockedMapper.when(() -> ProjectMapper.toDto(any(Project.class))).thenReturn(projectResponseDto);

            // Act
            ProjectResponseDto result = projectService.create(authentication, dto);

            // Assert
            Assertions.assertNotNull(result, "Result should not be null");
            Assertions.assertEquals(projectResponseDto, result, "DTOs should match");
        }
    }

    @Test
    public void shouldThrowAuthenticationFailedExceptionWhenUserNotAuthenticated() {
        // Arrange
        ProjectRequestDto dto = new ProjectRequestDto();

        when(userService.getCurrentUserOrThrow(authentication))
                .thenThrow(new AuthenticationFailedException("User not authenticated"));

        // Act & Assert
        assertThrows(AuthenticationFailedException.class, () -> projectService.create(authentication, dto));
    }

    @Test
    public void shouldIncludePartnerGroupsWhenCreatingPartnerOnlyProject() {
        // Arrange
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("Partner Project");
        dto.setPartnerOnly(true);
        dto.setPartnerGroupIds(List.of(1L));

        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(1L);

        Set<PartnerGroup> partnerGroups = new HashSet<>();
        partnerGroups.add(partnerGroup);
        company.setPartnerGroups(partnerGroups);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(user.getCompany().getId())).thenReturn(Optional.of(company));
        when(partnerGroupRepository.findAllById(dto.getPartnerGroupIds())).thenReturn(List.of(partnerGroup));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            mockedMapper.when(() -> ProjectMapper.toDto(any(Project.class))).thenReturn(projectResponseDto);

            // Act
            ProjectResponseDto result = projectService.create(authentication, dto);

            // Assert
            Assertions.assertNotNull(result, "Result should not be null");
            Assertions.assertEquals(projectResponseDto, result, "DTOs should match");
        }
    }

    @Test
    public void shouldThrowPermissionDeniedExceptionWhenPartnerGroupsDoNotBelongToCompany() {
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("Unauthorized Partner Project");
        dto.setPartnerOnly(true);
        dto.setPartnerGroupIds(List.of(1L));

        PartnerGroup validPartnerGroup = new PartnerGroup();
        validPartnerGroup.setId(2L);

        Set<PartnerGroup> partnerGroups = new HashSet<>();
        partnerGroups.add(validPartnerGroup);
        company.setPartnerGroups(partnerGroups);

        PartnerGroup invalidPartnerGroup = new PartnerGroup();
        invalidPartnerGroup.setId(1L);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(user.getCompany().getId())).thenReturn(Optional.of(company));
        when(partnerGroupRepository.findAllById(dto.getPartnerGroupIds())).thenReturn(List.of(invalidPartnerGroup));

        assertThrows(PermissionDeniedException.class, () -> projectService.create(authentication, dto));
    }
    @Test
    public void shouldDeleteProjectWhenValidDeleteRequest() {
        Long projectId = 1L;
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        projectService.delete(projectId, authentication);

        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenProjectNotFound() {
        Long projectId = 1L;
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> projectService.delete(projectId, authentication));
    }
    @Test
    public void shouldThrowPermissionDeniedExceptionWhenUserNotAuthorized() {
        Long projectId = 1L;
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Company anotherCompany = new Company();
        anotherCompany.setId(2L);
        project.setCompany(anotherCompany);

        assertThrows(PermissionDeniedException.class, () -> projectService.delete(projectId, authentication));
    }
    @Test
    public void shouldReturnPositionsWhenValidRequest() {
        Long projectId = 1L;
        User user = new User();
        Company userCompany = new Company();
        userCompany.setId(99999L);
        user.setCompany(userCompany);
        userCompany.setUsers(List.of(user));

        Project project = new Project();
        project.setId(projectId);
        project.setCompany(userCompany);
        project.setPositions(List.of(new Position(), new Position()));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(userCompany.getId())).thenReturn(Optional.of(userCompany));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        try (MockedStatic<PositionMapper> mockedMapper = mockStatic(PositionMapper.class)) {
            List<PositionResponseDto> responseDtos = List.of(new PositionResponseDto(), new PositionResponseDto());
            mockedMapper.when(() -> PositionMapper.toResponseDtoList(project.getPositions())).thenReturn(responseDtos);

            List<PositionResponseDto> result = projectService.getPositionsByProject(authentication, projectId);

            Assertions.assertNotNull(result, "Result should not be null");
            Assertions.assertEquals(2, result.size(), "Should return the correct number of positions");
        }
    }
    @Test
    public void shouldThrowNotFoundExceptionWhenNoPositionsAvailable() {
        Long projectId = 1L;
        User user = new User();
        Company userCompany = new Company();
        user.setCompany(userCompany);

        Project project = new Project();
        project.setId(projectId);
        project.setPositions(Collections.emptyList());

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(userCompany.getId())).thenReturn(Optional.of(userCompany));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(NotFoundException.class, () -> projectService.getPositionsByProject(authentication, projectId));
    }

    @Test
    public void shouldReactivateProjectWhenValidRequest() {
        Company userCompany = new Company();
        user.setCompany(userCompany);
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setCompany(userCompany);
        project.setProjectStatus(ProjectStatus.INACTIVE);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            ProjectResponseDto projectResponseDto = new ProjectResponseDto();
            mockedMapper.when(() -> ProjectMapper.toDto(project)).thenReturn(projectResponseDto);

            ProjectResponseDto result = projectService.reactivateProject(projectId, authentication);

            Assertions.assertNotNull(result, "Result should not be null");
            Assertions.assertEquals(projectResponseDto, result, "DTOs should match");
            Assertions.assertEquals(ProjectStatus.ACTIVE, project.getProjectStatus(), "Project status should be ACTIVE");
        }
    }
    @Test
    public void shouldThrowAlreadyExistsExceptionWhenProjectAlreadyActive() {
        Company userCompany = new Company();
        user.setCompany(userCompany);
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setCompany(userCompany);
        project.setProjectStatus(ProjectStatus.ACTIVE);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(AlreadyExistsException.class, () -> projectService.reactivateProject(projectId, authentication));
    }
    @Test
    public void shouldNotPerformAnyActionWhenNoExpiringOrExpiredProjects() {
        when(projectRepository.findProjectsExpiringInTwoDays()).thenReturn(Collections.emptyList());
        when(projectRepository.findProjectsOlderThan(21)).thenReturn(Collections.emptyList());

        projectService.processExpiringProjects();

        verify(mailService, never()).sendProjectExpiringMail(any(Project.class));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    public void shouldSendEmailsWhenExpiringProjectsFound() {
        Project expiringProject = new Project();
        expiringProject.setId(1L);
        expiringProject.setName("Expiring Project");

        List<Project> expiringProjects = Collections.singletonList(expiringProject);
        when(projectRepository.findProjectsExpiringInTwoDays()).thenReturn(expiringProjects);
        when(projectRepository.findProjectsOlderThan(21)).thenReturn(Collections.emptyList());

        projectService.processExpiringProjects();

        verify(mailService, times(1)).sendProjectExpiringMail(expiringProject);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    public void shouldUpdateStatusToInactiveWhenExpiredProjectsFound() {
        Project expiredProject = new Project();
        expiredProject.setId(2L);
        expiredProject.setProjectStatus(ProjectStatus.ACTIVE);

        List<Project> expiredProjects = Collections.singletonList(expiredProject);
        when(projectRepository.findProjectsExpiringInTwoDays()).thenReturn(Collections.emptyList());
        when(projectRepository.findProjectsOlderThan(21)).thenReturn(expiredProjects);

        projectService.processExpiringProjects();

        verify(mailService, never()).sendProjectExpiringMail(any(Project.class));
        verify(projectRepository, times(1)).save(expiredProject);
        Assertions.assertEquals(ProjectStatus.INACTIVE, expiredProject.getProjectStatus(), "Project status should be INACTIVE");
    }

    @Test
    public void shouldPerformBothActionsWhenBothExpiringAndExpiredProjectsFound() {
        Project expiringProject = new Project();
        expiringProject.setId(1L);
        expiringProject.setName("Expiring Project");

        Project expiredProject = new Project();
        expiredProject.setId(2L);
        expiredProject.setProjectStatus(ProjectStatus.ACTIVE);

        List<Project> expiringProjects = Collections.singletonList(expiringProject);
        List<Project> expiredProjects = Collections.singletonList(expiredProject);

        when(projectRepository.findProjectsExpiringInTwoDays()).thenReturn(expiringProjects);
        when(projectRepository.findProjectsOlderThan(21)).thenReturn(expiredProjects);

        projectService.processExpiringProjects();

        verify(mailService, times(1)).sendProjectExpiringMail(expiringProject);
        verify(projectRepository, times(1)).save(expiredProject);
        Assertions.assertEquals(ProjectStatus.INACTIVE, expiredProject.getProjectStatus(), "Project status should be INACTIVE");
    }
}