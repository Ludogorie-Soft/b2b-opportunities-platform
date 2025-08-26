package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.dto.requestDtos.ProjectRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.PositionResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.ProjectResponseDto;
import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.PartnerGroup;
import com.example.b2b_opportunities.entity.Pattern;
import com.example.b2b_opportunities.entity.Position;
import com.example.b2b_opportunities.entity.PositionApplication;
import com.example.b2b_opportunities.entity.Project;
import com.example.b2b_opportunities.entity.User;
import com.example.b2b_opportunities.exception.AuthenticationFailedException;
import com.example.b2b_opportunities.exception.common.InvalidRequestException;
import com.example.b2b_opportunities.exception.common.NotFoundException;
import com.example.b2b_opportunities.exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.mapper.PositionMapper;
import com.example.b2b_opportunities.mapper.ProjectMapper;
import com.example.b2b_opportunities.repository.CompanyRepository;
import com.example.b2b_opportunities.repository.PartnerGroupRepository;
import com.example.b2b_opportunities.repository.PositionApplicationRepository;
import com.example.b2b_opportunities.repository.PositionRepository;
import com.example.b2b_opportunities.repository.ProjectRepository;
import com.example.b2b_opportunities.services.impl.CompanyServiceImpl;
import com.example.b2b_opportunities.services.impl.EmailSchedulerServiceImpl;
import com.example.b2b_opportunities.services.impl.ProjectServiceImpl;
import com.example.b2b_opportunities.services.interfaces.EmailDailyStatsService;
import com.example.b2b_opportunities.services.interfaces.MailService;
import com.example.b2b_opportunities.services.interfaces.PositionApplicationService;
import com.example.b2b_opportunities.services.interfaces.UserService;
import com.example.b2b_opportunities.enums.ApplicationStatus;
import com.example.b2b_opportunities.enums.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private CompanyServiceImpl companyService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Mock
    private PositionApplicationService positionApplicationService;

    @Mock
    PositionApplicationRepository positionApplicationRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private EmailSchedulerServiceImpl emailSchedulerService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PartnerGroupRepository partnerGroupRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private EmailDailyStatsService emailDailyStatsService;


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
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            mockedMapper.when(() -> ProjectMapper.toDto(project)).thenReturn(projectResponseDto);
            ProjectResponseDto result = projectService.get(authentication, 1L);

            assertNotNull(result, "Result should not be null");
            assertEquals(projectResponseDto, result, "DTOs should match");
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
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);

        project.setPartnerOnly(true);
        Company projectCompany = new Company();
        projectCompany.setId(99999999L);
        project.setCompany(projectCompany);
        PartnerGroup partnerGroup = new PartnerGroup();
        Company partnerCompany = new Company();
        partnerCompany.setId(3L);
        partnerGroup.setPartners(Collections.singleton(partnerCompany));

        project.setPartnerGroupList(Collections.singleton(partnerGroup));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(PermissionDeniedException.class, () -> projectService.get(authentication, 1L));
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

            assertNotNull(result, "Result should not be null");
            assertEquals(projectResponseDto, result, "DTOs should match");
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
        dto.setPartnerGroups(List.of(1L));

        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(1L);
        Set<PartnerGroup> partnerGroups = new HashSet<>();
        partnerGroups.add(partnerGroup);

        company.setPartnerGroups(partnerGroups);
        project.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(partnerGroupRepository.findAllById(dto.getPartnerGroups())).thenReturn(List.of(partnerGroup));

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            mockedMapper.when(() -> ProjectMapper.toDto(any(Project.class))).thenReturn(projectResponseDto);

            ProjectResponseDto result = projectService.update(projectId, dto, authentication);

            assertNotNull(result, "Result should not be null");
            assertEquals(projectResponseDto, result, "DTOs should match");
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
            assertNotNull(result, "Result should not be null");
            assertEquals(projectResponseDto, result, "DTOs should match");
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
        dto.setPartnerGroups(List.of(1L));

        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(1L);

        Set<PartnerGroup> partnerGroups = new HashSet<>();
        partnerGroups.add(partnerGroup);
        company.setPartnerGroups(partnerGroups);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(user.getCompany().getId())).thenReturn(Optional.of(company));
        when(partnerGroupRepository.findAllById(dto.getPartnerGroups())).thenReturn(List.of(partnerGroup));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);

        try (MockedStatic<ProjectMapper> mockedMapper = mockStatic(ProjectMapper.class)) {
            mockedMapper.when(() -> ProjectMapper.toDto(any(Project.class))).thenReturn(projectResponseDto);

            // Act
            ProjectResponseDto result = projectService.create(authentication, dto);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(projectResponseDto, result, "DTOs should match");
        }
    }

    @Test
    public void shouldThrowPermissionDeniedExceptionWhenPartnerGroupsDoNotBelongToCompany() {
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("Unauthorized Partner Project");
        dto.setPartnerOnly(true);
        dto.setPartnerGroups(List.of(1L));

        PartnerGroup validPartnerGroup = new PartnerGroup();
        validPartnerGroup.setId(2L);

        Set<PartnerGroup> partnerGroups = new HashSet<>();
        partnerGroups.add(validPartnerGroup);
        company.setPartnerGroups(partnerGroups);

        PartnerGroup invalidPartnerGroup = new PartnerGroup();
        invalidPartnerGroup.setId(1L);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(user.getCompany().getId())).thenReturn(Optional.of(company));
        when(partnerGroupRepository.findAllById(dto.getPartnerGroups())).thenReturn(List.of(invalidPartnerGroup));
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);

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
        userCompany.setApproved(true);
        user.setCompany(userCompany);
        userCompany.setUsers(List.of(user));

        Project project = new Project();
        project.setId(projectId);
        project.setCompany(userCompany);
        project.setPositions(List.of(new Position(), new Position()));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(userCompany.getId())).thenReturn(Optional.of(userCompany));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);

        try (MockedStatic<PositionMapper> mockedMapper = mockStatic(PositionMapper.class)) {
            List<PositionResponseDto> responseDtos = List.of(new PositionResponseDto(), new PositionResponseDto());
            mockedMapper.when(() -> PositionMapper.toResponseDtoList(project.getPositions())).thenReturn(responseDtos);

            List<PositionResponseDto> result = projectService.getPositionsByProject(authentication, projectId);

            assertNotNull(result, "Result should not be null");
            assertEquals(2, result.size(), "Should return the correct number of positions");
        }
    }

    @Test
    public void shouldReturnEmptyListWhenNoPositionsAvailable() {
        Long projectId = 1L;
        User user = new User();
        Company userCompany = new Company();
        user.setCompany(userCompany);

        Project project = new Project();
        project.setId(projectId);
        project.setPositions(Collections.emptyList());
        project.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(userCompany.getId())).thenReturn(Optional.of(userCompany));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);

        assertEquals(projectService.getPositionsByProject(authentication, projectId), new ArrayList<>());
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

            assertNotNull(result, "Result should not be null");
            assertEquals(projectResponseDto, result, "DTOs should match");
            assertEquals(ProjectStatus.ACTIVE, project.getProjectStatus(), "Project status should be ACTIVE");
        }
    }

    @Test
    public void shouldThrowInvalidRequestExceptionWhenProjectAlreadyActive() {
        Company userCompany = new Company();
        user.setCompany(userCompany);
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setCompany(userCompany);
        project.setProjectStatus(ProjectStatus.ACTIVE);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(InvalidRequestException.class, () -> projectService.reactivateProject(projectId, authentication));
    }

    @Test
    public void shouldNotPerformAnyActionWhenNoExpiringOrExpiredProjects() {
        when(projectRepository.findProjectsExpiringInTwoDays()).thenReturn(Collections.emptyList());
        when(projectRepository.findExpiredAndActiveProjects()).thenReturn(Collections.emptyList());
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);

        emailSchedulerService.processExpiringProjects();

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
        when(projectRepository.findExpiredAndActiveProjects()).thenReturn(Collections.emptyList());
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);

        emailSchedulerService.processExpiringProjects();

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
        when(projectRepository.findExpiredAndActiveProjects()).thenReturn(expiredProjects);

        emailSchedulerService.processExpiringProjects();

        verify(mailService, never()).sendProjectExpiringMail(any(Project.class));
        verify(projectRepository, times(1)).save(expiredProject);
        assertEquals(ProjectStatus.INACTIVE, expiredProject.getProjectStatus(), "Project status should be INACTIVE");
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
        when(projectRepository.findExpiredAndActiveProjects()).thenReturn(expiredProjects);

        emailSchedulerService.processExpiringProjects();

        verify(mailService, times(1)).sendProjectExpiringMail(expiringProject);
        verify(projectRepository, times(1)).save(expiredProject);
        assertEquals(ProjectStatus.INACTIVE, expiredProject.getProjectStatus(), "Project status should be INACTIVE");
    }

    @Test
    void shouldGetProjectAndItsViewsAndApplicationsInfo() {
        User user = new User();
        Company company = Company.builder().id(1L).build();
        user.setCompany(company);

        Project project = new Project();
        project.setId(1L);
        project.setPartnerOnly(false);
        project.setCompany(company);
        project.setProjectStatus(ProjectStatus.ACTIVE);

        Position position = new Position();
        position.setId(1L);
        position.setViews(2L);
        Position position2 = new Position();
        position2.setId(2L);
        position2.setViews(2L);

        PositionApplication pa = new PositionApplication();
        pa.setId(1L);
        pa.setPosition(position);
        pa.setApplicationStatus(ApplicationStatus.IN_PROGRESS); //One in progress
        PositionApplication pa2 = new PositionApplication();
        pa2.setId(2L);
        pa2.setPosition(position);
        pa.setApplicationStatus(ApplicationStatus.ACCEPTED); //And one accepted

        project.setPositions(List.of(position, position2));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(positionApplicationRepository.countByPositionIdAndApplicationStatus(1L, ApplicationStatus.ACCEPTED))
                .thenReturn(1L);
        when(positionApplicationRepository.countByPositionIdAndApplicationStatus(2L, ApplicationStatus.ACCEPTED))
                .thenReturn(0L);

        when(positionApplicationRepository.countByPositionIdExcludingAwaitingCvOrTalent(1L))
                .thenReturn(2L);
        when(positionApplicationRepository.countByPositionIdExcludingAwaitingCvOrTalent(2L))
                .thenReturn(0L);

        ProjectResponseDto responseDto = projectService.get(authentication, 1L);

        assertEquals(4L, responseDto.getPositionViews());
        assertEquals(1L, responseDto.getAcceptedApplications());
        assertEquals(2L, responseDto.getTotalApplications());
    }

    @Test
    void testGetProjectByUnapprovedCompanyShouldThrowException(){
        User user = new User();
        Company company = Company.builder().id(1L).build();

        Company anotherCompany = Company.builder().id(2L).build();
        anotherCompany.setApproved(false);

        Project project = new Project();
        project.setId(1L);
        project.setPartnerOnly(false);
        project.setCompany(anotherCompany);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));

        PermissionDeniedException exception = assertThrows(PermissionDeniedException.class, () ->
                projectService.get(authentication, 1L));

        assertEquals(exception.getMessage(), "Project posted by an unapproved company");
    }

    @Test
    void getPartnerOnlyProjectWhenProjectIsInactiveShouldThrowException(){
        User user = new User();
        Company company = Company.builder().id(1L).build();

        Company anotherCompany = Company.builder().id(2L).build();

        Project project = new Project();
        project.setId(1L);
        project.setPartnerOnly(true);
        project.setCompany(anotherCompany);
        PartnerGroup pg = PartnerGroup.builder()
                .partners(Set.of(company))
                .build();
        project.setPartnerGroupList(Set.of(pg));
        project.setProjectStatus(ProjectStatus.INACTIVE);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));

        PermissionDeniedException exception = assertThrows(PermissionDeniedException.class, () ->
                projectService.get(authentication, 1L));

        assertEquals(exception.getMessage(), "This project is inactive");
    }

    @Test
    void getPartnerProjectWhenCompanyIsUnapprovedShouldThrowException(){
        User user = new User();
        Company company = Company.builder().id(1L).build();

        Company anotherCompany = Company.builder().id(2L).build();
        anotherCompany.setApproved(false);

        Project project = new Project();
        project.setId(1L);
        project.setPartnerOnly(true);
        project.setCompany(anotherCompany);
        PartnerGroup pg = PartnerGroup.builder()
                .partners(Set.of(company))
                .build();
        project.setPartnerGroupList(Set.of(pg));
        project.setProjectStatus(ProjectStatus.ACTIVE);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                projectService.get(authentication, 1L));

        assertEquals(exception.getMessage(), "The company has not yet been approved to post public projects");
    }


    @Test
    void createOrUpdateShouldThrowExceptionWhenPartnerGroupNotFound(){
        User user = new User();
        Company company = Company.builder().id(1L).build();

        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("test");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());
        dto.setDuration(5);
        dto.setDescription("test");
        dto.setPartnerOnly(true);
        dto.setPartnerGroups(List.of(1L, 2L));

        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(1L);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(partnerGroupRepository.findAllById(anyList())).thenReturn(List.of(partnerGroup));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                projectService.create(authentication, dto));

        assertEquals(exception.getMessage(), "PartnerGroups with ID(s) [2] not found.");
    }

    @Test
    void testProcessNewApplicationsWithNoApplications() {

        when(positionApplicationService.getApplicationsSinceLastWorkday()).thenReturn(Collections.emptyList());

        emailSchedulerService.processNewApplications();

        verify(mailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testProcessNewApplicationsWithApplications() {
        Company company = new Company();
        company.setEmail("company@example.com");
        Position position = new Position();
        position.setPattern(Pattern.builder().name("Position A").build());
        Project project = new Project();
        project.setCompany(company);
        position.setProject(project);
        PositionApplication application = new PositionApplication();
        application.setPosition(position);
        application.setApplicationDateTime(LocalDateTime.now());

        List<PositionApplication> positionApplications = Collections.singletonList(application);

        when(positionApplicationService.getApplicationsSinceLastWorkday()).thenReturn(positionApplications);

        emailSchedulerService.processNewApplications();

        verify(mailService, times(1)).sendEmail(eq("company@example.com"), anyString(), eq("New Job Applications for Your Positions"));
    }

    @Test
    void testProcessNewApplicationsWithmultipleApplications() {
        Company company = new Company();
        company.setEmail("company@example.com");

        Position positionA = new Position();
        positionA.setPattern(Pattern.builder().name("Position A").build());

        Position positionB = new Position();
        positionB.setPattern(Pattern.builder().name("Position B").build());

        Project project = new Project();
        project.setCompany(company);

        positionA.setProject(project);
        positionB.setProject(project);

        PositionApplication applicationA = new PositionApplication();
        applicationA.setPosition(positionA);
        applicationA.setApplicationDateTime(LocalDateTime.now());

        PositionApplication applicationB = new PositionApplication();
        applicationB.setPosition(positionB);
        applicationB.setApplicationDateTime(LocalDateTime.now());

        List<PositionApplication> positionApplications = Arrays.asList(applicationA, applicationB);

        when(positionApplicationService.getApplicationsSinceLastWorkday()).thenReturn(positionApplications);

        emailSchedulerService.processNewApplications();

        verify(mailService, times(1)).sendEmail(eq("company@example.com"), anyString(), eq("New Job Applications for Your Positions"));
    }

}