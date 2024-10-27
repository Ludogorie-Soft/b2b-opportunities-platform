package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Mapper.ProjectMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
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
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


public class ProjectServiceTest {

    @Mock
    private UserService userService;

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



}