package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyFilterEditDto;
import com.example.b2b_opportunities.Dto.Request.CompanyFilterRequestDto;
import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Request.PartnerGroupRequestDto;
import com.example.b2b_opportunities.Dto.Request.SkillExperienceRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentExperienceRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentPublicityRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyFilterResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyPublicResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Dto.Response.PartnerGroupResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentPublicityResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.Filter;
import com.example.b2b_opportunities.Entity.Location;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Pattern;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.Seniority;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.SkillExperience;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.TalentExperience;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Entity.WorkMode;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Repository.DomainRepository;
import com.example.b2b_opportunities.Repository.FilterRepository;
import com.example.b2b_opportunities.Repository.LocationRepository;
import com.example.b2b_opportunities.Repository.PartnerGroupRepository;
import com.example.b2b_opportunities.Repository.PatternRepository;
import com.example.b2b_opportunities.Repository.PositionApplicationRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Repository.SeniorityRepository;
import com.example.b2b_opportunities.Repository.SkillExperienceRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import com.example.b2b_opportunities.Repository.TalentExperienceRepository;
import com.example.b2b_opportunities.Repository.TalentRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Repository.WorkModeRepository;
import com.example.b2b_opportunities.Service.Implementation.CompanyServiceImpl;
import com.example.b2b_opportunities.Service.Implementation.ImageServiceImpl;
import com.example.b2b_opportunities.Service.Interface.MailService;
import com.example.b2b_opportunities.Service.Interface.PatternService;
import com.example.b2b_opportunities.Service.Interface.UserService;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.Static.ProjectStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompanyServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MailService mailService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private PositionApplicationRepository positionApplicationRepository;

    @Mock
    private ImageServiceImpl imageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatternService patternService;

    @Mock
    private FilterRepository filterRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SeniorityRepository seniorityRepository;

    @Mock
    private SkillExperienceRepository skillExperienceRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private WorkModeRepository workModeRepository;

    @Mock
    private PatternRepository patternRepository;

    @Mock
    private CompanyTypeRepository companyTypeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private TalentExperienceRepository talentExperienceRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PartnerGroupRepository partnerGroupRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private CompanyRequestDto companyRequestDto;

    private User currentUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        companyRequestDto = new CompanyRequestDto();
        companyRequestDto.setCompanyTypeId(999L);
        companyRequestDto.setDomainId(9999L);
        companyRequestDto.setEmail("test@test.com");
        currentUser = new User();

        CompanyType mockCompanyType = new CompanyType();
        mockCompanyType.setId(999L);
        mockCompanyType.setName("Test");

        Domain mockDomain = new Domain();
        mockDomain.setId(9999L);
        mockDomain.setName("Test");

        when(companyTypeRepository.findById(999L)).thenReturn(Optional.of(mockCompanyType));
        when(domainRepository.findById(9999L)).thenReturn(Optional.of(mockDomain));

        currentUser = new User();
        currentUser.setEmail("user@example.com");

        doNothing().when(mailService).sendCompanyEmailConfirmation(any(Company.class), any(String.class), any(HttpServletRequest.class));
    }

    @Test
    void testCreateCompanyThrowsAuthenticationFailedException() {
        when(userService.getCurrentUserOrThrow(null))
                .thenThrow(new AuthenticationFailedException("User not authenticated"));
        assertThrows(AuthenticationFailedException.class, () -> companyService.createCompany(null, companyRequestDto, request));
    }

    @Test
    void shouldThrowInvalidRequestExceptionWhenUserAlreadyHasCompany() {
        Company existingCompany = new Company();
        existingCompany.setName("Existing Company");
        currentUser.setCompany(existingCompany);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        assertThrows(InvalidRequestException.class, () -> companyService.createCompany(authentication, companyRequestDto, request));
    }

    @Test
    void ShouldCreateCompanyWhenValidRequest() {
        Company company = new Company();
        company.setId(1L);
        company.setEmailVerification(EmailVerification.ACCEPTED);

        company.setSkills(new HashSet<>());

        currentUser.setCompany(null);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        doNothing().when(mailService).sendCompanyEmailConfirmation(any(Company.class), any(String.class), any(HttpServletRequest.class));

        CompanyResponseDto responseDto = companyService.createCompany(authentication, companyRequestDto, request);

        verify(companyRepository, times(3)).save(any(Company.class));
        assertNotNull(responseDto);
    }

    @Test
    void shouldGetCompanyAndUsersSuccessfully() {
        Long companyId = 1L;
        Company company = new Company();
        company.setId(companyId);

        User user1 = new User();
        User user2 = new User();
        company.setUsers(Arrays.asList(user1, user2));
        company.setSkills(new HashSet<>());
        company.setEmailVerification(EmailVerification.ACCEPTED);

        CompanyResponseDto expectedResponseDto = new CompanyResponseDto();
        expectedResponseDto.setId(companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompaniesAndUsersResponseDto result = companyService.getCompanyAndUsers(companyId);

        assertNotNull(result);
        assertEquals(2, result.getUsers().size());
        assertNotNull(result.getCompany());
        assertEquals(expectedResponseDto.getId(), result.getCompany().getId());

        verify(companyRepository).findById(companyId);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCompanyDoesNotExist() {
        Long companyId = 999L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> companyService.getCompanyAndUsers(companyId));

        assertEquals("Company with ID: " + companyId + " not found", thrown.getMessage());
        verify(companyRepository).findById(companyId);
    }

    @Test
    void shouldReturnEmptyListWhenCompanyHasNoUsers() {
        Long companyId = 1L;
        Company company = new Company();
        company.setId(companyId);
        company.setUsers(new ArrayList<>());
        company.setSkills(new HashSet<>());
        company.setEmailVerification(EmailVerification.ACCEPTED);

        CompanyResponseDto expectedResponseDto = new CompanyResponseDto();
        expectedResponseDto.setId(companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompaniesAndUsersResponseDto result = companyService.getCompanyAndUsers(companyId);

        assertNotNull(result);
        assertEquals(0, result.getUsers().size());
        assertNotNull(result.getCompany());
        assertEquals(expectedResponseDto.getId(), result.getCompany().getId());
        verify(companyRepository).findById(companyId);
    }

    @Test
    void shouldReturnListOfUsersWhenCompanyHasUsers() {
        Long companyId = 2L;
        Company company = new Company();
        company.setId(companyId);

        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        company.setUsers(Arrays.asList(user1, user2, user3));
        company.setSkills(new HashSet<>());
        company.setEmailVerification(EmailVerification.ACCEPTED);

        CompanyResponseDto expectedResponseDto = new CompanyResponseDto();
        expectedResponseDto.setId(companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompaniesAndUsersResponseDto result = companyService.getCompanyAndUsers(companyId);

        assertNotNull(result);
        assertEquals(3, result.getUsers().size());
        assertNotNull(result.getCompany());
        assertEquals(expectedResponseDto.getId(), result.getCompany().getId());

        verify(companyRepository).findById(companyId);
    }

    @Test
    void shouldConfirmCompanyWithValidToken() {
        Long companyId = 9999L;
        Company company = new Company();
        company.setId(companyId);
        company.setEmailConfirmationToken("test123");

        when(companyRepository.findByEmailConfirmationToken("test123")).thenReturn(Optional.of(company));

        companyService.confirmCompanyEmail("test123");

        assertEquals(company.getEmailVerification().toString(), "ACCEPTED");
        assertNull(company.getEmailConfirmationToken());

        verify(companyRepository).save(company);
    }

    @Test
    void shouldNotConfirmCompanyEmailWithInvalidToken() {
        Long companyId = 9999L;
        Company company = new Company();
        company.setId(companyId);
        company.setEmailVerification(EmailVerification.PENDING);
        company.setEmailConfirmationToken("test123");
        String invalidToken = "test321";

        NotFoundException expectedException = assertThrows(NotFoundException.class, () ->
                companyService.confirmCompanyEmail(invalidToken));

        assertEquals("Invalid or already used token", expectedException.getMessage());
        assertEquals(company.getEmailVerification().toString(), "PENDING");
        assertNotNull(company.getEmailConfirmationToken());
    }


    @Test
    void shouldEditCompanySuccessfully() {
        CompanyType companyType = new CompanyType();
        companyType.setId(99999L);
        companyType.setName("testCT");

        Set<Skill> skillSet = new HashSet<>();
        Skill skill = Skill.builder().id(9999L).build();
        skillSet.add(skill);

        List<Long> skillList = List.of(9999L);

        Domain domain = new Domain();
        domain.setId(9999L);
        domain.setName("testDomain");
        User currentUser = new User();
        currentUser.setId(9999L);
        Company userCompany = new Company();
        userCompany.setId(9999L);
        currentUser.setCompany(userCompany);
        currentUser.setEmail("user@example.com");
        userCompany.setUsers(List.of(currentUser));
        userCompany.setEmail("oldemail@example.com");
        userCompany.setWebsite("oldwebsite.com");
        userCompany.setLinkedIn("oldlinkedin.com");
        userCompany.setCompanyType(companyType);
        userCompany.setDomain(domain);
        userCompany.setSkills(skillSet);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(currentUser.getId())).thenReturn(Optional.of(userCompany));

        when(companyRepository.save(any(Company.class))).thenReturn(userCompany);

        companyRequestDto.setName("New Company Name");
        companyRequestDto.setEmail("newemail@example.com");
        companyRequestDto.setWebsite("newwebsite.com");
        companyRequestDto.setLinkedIn("newlinkedin.com");
        companyRequestDto.setCompanyTypeId(companyType.getId());
        companyRequestDto.setDomainId(domain.getId());
        companyRequestDto.setSkills(skillList);

        CompanyResponseDto responseDto = companyService.editCompany(authentication, companyRequestDto, request);

        assertNotNull(responseDto);
        assertEquals("New Company Name", responseDto.getName());
        assertEquals("newemail@example.com", userCompany.getEmail());
        assertEquals("newwebsite.com", userCompany.getWebsite());
        verify(companyRepository, times(2)).save(userCompany);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserHasNoCompany() {
        User currentUser = new User();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(currentUser.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.editCompany(authentication, companyRequestDto, request));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllProjectsForUserCompany() {
        User currentUser = new User();
        currentUser.setId(99999999L);
        Company userCompany = new Company();
        userCompany.setId(1L);
        currentUser.setCompany(userCompany);

        Company company = new Company();
        company.setId(1L);
        ProjectStatus projectStatus = ProjectStatus.ACTIVE;
        Project project1 = new Project();
        Project project2 = new Project();
        project1.setCompany(company);
        project1.setProjectStatus(projectStatus);
        project2.setCompany(company);
        project2.setProjectStatus(projectStatus);
        List<Project> projects = List.of(project1, project2);
        company.setProjects(projects);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        Set<ProjectResponseDto> result = companyService.getCompanyProjects(authentication, 1L);

        assertEquals(2, result.size());
        verify(companyRepository).findById(1L);
        verify(userService).getCurrentUserOrThrow(authentication);
    }

    @Test
    void shouldReturnEmptySetForUnapprovedCompany() {
        User currentUser = new User();
        currentUser.setId(99999999L);
        Company userCompany = new Company();
        userCompany.setId(1L);
        currentUser.setCompany(userCompany);

        Company company = new Company();
        company.setId(2L);
        company.setApproved(false);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(2L)).thenReturn(Optional.of(company));

        Set<ProjectResponseDto> result = companyService.getCompanyProjects(authentication, 2L);

        assertTrue(result.isEmpty());
        verify(companyRepository).findById(2L);
        verify(userService).getCurrentUserOrThrow(authentication);
    }

    @Test
    void shouldReturnOnlyPublicActiveProjectsForApprovedCompany() {
        User currentUser = new User();
        currentUser.setId(99999999L);
        Company userCompany = new Company();
        userCompany.setId(1L);
        currentUser.setCompany(userCompany);

        Company company = new Company();
        company.setId(2L);
        company.setApproved(true);

        Project publicProject1 = new Project();
        publicProject1.setCompany(company);
        publicProject1.setProjectStatus(ProjectStatus.ACTIVE);
        Project publicProject2 = new Project();
        publicProject2.setCompany(company);
        publicProject2.setProjectStatus(ProjectStatus.ACTIVE);

        List<Project> publicProjects = List.of(publicProject1, publicProject2);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(2L)).thenReturn(Optional.of(company));
        when(projectRepository.findActiveNonPartnerOnlyProjectsByCompanyId(ProjectStatus.ACTIVE, 2L))
                .thenReturn(publicProjects);
        when(projectRepository.findActivePartnerOnlyProjectsSharedWithCompany(ProjectStatus.ACTIVE, 2L, 1L))
                .thenReturn(List.of());

        Set<ProjectResponseDto> result = companyService.getCompanyProjects(authentication, 2L);

        assertEquals(2, result.size());
        verify(projectRepository).findActiveNonPartnerOnlyProjectsByCompanyId(ProjectStatus.ACTIVE, 2L);
        verify(projectRepository).findActivePartnerOnlyProjectsSharedWithCompany(ProjectStatus.ACTIVE, 2L, 1L);
    }

    @Test
    void shouldReturnPublicAndPartnerProjectsForApprovedCompany() {
        User currentUser = new User();
        currentUser.setId(99999999L);
        Company userCompany = new Company();
        userCompany.setId(1L);
        currentUser.setCompany(userCompany);

        Company company = new Company();
        company.setId(2L);
        company.setApproved(true);

        Project publicProject = new Project();
        publicProject.setCompany(company);
        publicProject.setProjectStatus(ProjectStatus.ACTIVE);
        Project partnerProject = new Project();
        partnerProject.setCompany(company);
        partnerProject.setProjectStatus(ProjectStatus.ACTIVE);

        List<Project> publicProjects = List.of(publicProject);
        List<Project> partnerProjects = List.of(partnerProject);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(2L)).thenReturn(Optional.of(company));
        when(projectRepository.findActiveNonPartnerOnlyProjectsByCompanyId(ProjectStatus.ACTIVE, 2L))
                .thenReturn(publicProjects);
        when(projectRepository.findActivePartnerOnlyProjectsSharedWithCompany(ProjectStatus.ACTIVE, 2L, 1L))
                .thenReturn(partnerProjects);

        Set<ProjectResponseDto> result = companyService.getCompanyProjects(authentication, 2L);

        assertEquals(2, result.size());
        verify(projectRepository).findActiveNonPartnerOnlyProjectsByCompanyId(ProjectStatus.ACTIVE, 2L);
        verify(projectRepository).findActivePartnerOnlyProjectsSharedWithCompany(ProjectStatus.ACTIVE, 2L, 1L);
    }

    @Test
    void shouldThrowExceptionWhenQueriedCompanyNotFound() {
        when(companyRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.getCompanyProjects(authentication, 3L));
        verify(companyRepository).findById(3L);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationIsNull() {
        assertThrows(NotFoundException.class, () -> companyService.getCompanyProjects(null, 1L));
    }

    @Test
    void shouldReturnCompanyFilters() {
        User currentUser = new User();
        currentUser.setId(99999999L);
        Company userCompany = new Company();
        userCompany.setId(1L);
        currentUser.setCompany(userCompany);

        Skill skill = new Skill();
        skill.setId(55L);

        Filter testFilter = new Filter();
        testFilter.setId(9999L);
        testFilter.setCompany(userCompany);
        testFilter.setSkills(Set.of(skill));

        Filter testFilter2 = new Filter();
        testFilter2.setId(99999L);
        testFilter2.setCompany(userCompany);
        testFilter2.setSkills(Set.of(skill));

        Set<Filter> filters = Set.of(testFilter, testFilter2);
        userCompany.setFilters(filters);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        List<CompanyFilterResponseDto> result = companyService.getCompanyFilters(authentication);

        assertEquals(2, result.size());
        verify(userService).getCurrentUserOrThrow(authentication);
    }

    @Test
    void shouldReturnCompanyFilter() {
        User currentUser = new User();
        currentUser.setId(99999999L);
        Company userCompany = new Company();
        userCompany.setId(1L);
        currentUser.setCompany(userCompany);

        Skill skill = new Skill();
        skill.setId(55L);

        Filter testFilter = new Filter();
        testFilter.setId(9999L);
        testFilter.setName("testFilter");
        testFilter.setCompany(userCompany);
        testFilter.setSkills(Set.of(skill));

        Set<Filter> filters = Set.of(testFilter);
        userCompany.setFilters(filters);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(filterRepository.findById(9999L)).thenReturn(Optional.of(testFilter));

        CompanyFilterResponseDto result = companyService.getCompanyFilter(9999L, authentication);

        assertEquals("testFilter", result.getName());
        assertEquals(9999L, result.getId());
        assertEquals(1, result.getSkills().size());
        assertTrue(result.getSkills().contains(55L));
    }

    @Test
    void testShouldThrowAlreadyExistsExceptionWhenEmailAlreadyRegistered() {
        when(companyRepository.findByEmail(companyRequestDto.getEmail())).thenReturn(Optional.of(new Company()));
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        assertThrows(AlreadyExistsException.class, () -> companyService.createCompany(authentication, companyRequestDto, request));

        verify(companyRepository, never()).save(any());
    }


    @Test
    void testShouldThrowAlreadyExistsExceptionWhenLinkedInAlreadyRegistered() {
        companyRequestDto.setLinkedIn("duplicateLinkedIn");
        when(companyRepository.findByLinkedIn("duplicateLinkedIn")).thenReturn(Optional.of(new Company()));
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        assertThrows(AlreadyExistsException.class, () -> companyService.createCompany(authentication, companyRequestDto, request));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testShouldThrowNotFoundExceptionWhenEmailTokenIsInvalid() {
        when(companyRepository.findByEmailConfirmationToken("invalidToken")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.confirmCompanyEmail("invalidToken"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void testShouldThrowNotFoundExceptionWhenCompanyBannerDoesNotExist() {
        Company company = new Company();
        company.setId(1L);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(imageService.doesImageExist(company.getId(), "banner")).thenReturn(false);

        assertThrows(NotFoundException.class, () -> companyService.deleteCompanyBanner(authentication));
    }

    @Test
    void testShouldDeleteCompanyBannerSuccessfullyWhenItExists() {
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(imageService.doesImageExist(company.getId(), "banner")).thenReturn(true);

        doNothing().when(imageService).deleteBanner(company.getId());

        companyService.deleteCompanyBanner(authentication);

        verify(imageService, times(1)).deleteBanner(company.getId());
    }

    @Test
    void testShouldThrowNotFoundExceptionWhenCompanyImageDoesNotExist() {
        Company company = new Company();
        company.setId(1L);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(imageService.doesImageExist(company.getId(), "companyImage")).thenReturn(false);

        assertThrows(NotFoundException.class, () -> companyService.deleteCompanyBanner(authentication));
    }

    @Test
    void testShouldThrowNotFoundExceptionWhenFilterNotRelatedToCompany() {
        Company company = new Company();
        company.setId(1L);
        company.setFilters(new HashSet<>());
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(filterRepository.findById(999L)).thenReturn(Optional.of(new Filter()));

        assertThrows(NotFoundException.class, () -> companyService.getCompanyFilter(999L, authentication));
    }

    @Test
    void testEditCompanyFilterWithInvalidId() {
        Long invalidId = -1L;
        CompanyFilterEditDto dto = new CompanyFilterEditDto();
        Authentication authentication = mock(Authentication.class);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(new User());

        assertThrows(NotFoundException.class, () ->
                companyService.editCompanyFilter(invalidId, dto, authentication)
        );
    }

    @Test
    void testDeleteCompanyFilterWithInvalidId() {
        Long invalidId = -1L;
        Authentication authentication = mock(Authentication.class);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(new User());

        assertThrows(NotFoundException.class, () ->
                companyService.deleteCompanyFilter(invalidId, authentication)
        );
    }

    @Test
    void testAddCompanyFilterWithInvalidCompanyId() {
        Authentication authentication = mock(Authentication.class);
        CompanyFilterRequestDto dto = new CompanyFilterRequestDto();

        User user = new User();
        Company company = new Company();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findById(anyLong())).thenReturn(Optional.of(company));

        assertThrows(NotFoundException.class, () ->
                companyService.addCompanyFilter(authentication, dto)
        );
    }

    @Test
    void testShouldSetCompanyImagesSuccessfully() {
        Company company = new Company();
        company.setId(1L);

        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("testSkill");
        company.setSkills(Set.of(skill));
        company.setEmailVerification(EmailVerification.ACCEPTED);

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setCompany(company);

        MultipartFile image = mock(MultipartFile.class);
        MultipartFile banner = mock(MultipartFile.class);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        when(imageService.upload(image, company.getId(), "image")).thenReturn("imageUploaded");
        when(imageService.upload(banner, company.getId(), "banner")).thenReturn("bannerUploaded");

        CompanyResponseDto mockResponseDto = new CompanyResponseDto();
        mockResponseDto.setImage("imageUploaded");
        mockResponseDto.setBanner("bannerUploaded");

        CompanyResponseDto result = companyService.setCompanyImages(authentication, image, banner);

        verify(imageService, times(1)).upload(image, company.getId(), "image");
        verify(imageService, times(1)).upload(banner, company.getId(), "banner");

        assertNotNull(result);
    }

    @Test
    void testShouldReturnCompanyResponseDtoSuccessfully() {
        Company company = new Company();
        company.setId(1L);

        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("testSkill");
        company.setSkills(Set.of(skill));
        company.setEmailVerification(EmailVerification.ACCEPTED);

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setCompany(company);

        CompanyResponseDto mockResponseDto = new CompanyResponseDto();
        mockResponseDto.setName("Test Company");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        when(imageService.returnUrlIfPictureExists(company.getId(), "image")).thenReturn("http://image.url");
        when(imageService.returnUrlIfPictureExists(company.getId(), "banner")).thenReturn("http://banner.url");

        CompanyResponseDto result = companyService.getCompany(1L);

        verify(companyRepository, times(1)).findById(1L);

        assertNotNull(result);
        assertEquals("http://image.url", result.getImage());
        assertEquals("http://banner.url", result.getBanner());
    }


    @Test
    void testShouldThrowNotFoundExceptionWhenCompanyNotFound() {
        when(companyRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.getCompany(9999L));

        verify(companyRepository, times(1)).findById(9999L);
    }

    @Test
    void testEditCompanyFilterSuccessfully() {
        Long filterId = 1L;
        CompanyFilterEditDto dto = new CompanyFilterEditDto();
        dto.setSkills(Set.of(1L, 2L));
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        Filter filter = new Filter();
        filter.setId(filterId);
        filter.setCompany(company);
        filter.setSkills(Set.of(new Skill(), new Skill()));
        company.setFilters(Set.of(filter));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(filterRepository.findById(filterId)).thenReturn(Optional.of(filter));
        when(filterRepository.save(any(Filter.class))).thenReturn(filter);

        CompanyFilterResponseDto response = companyService.editCompanyFilter(filterId, dto, authentication);

        assertNotNull(response);
        assertEquals(filterId, response.getId());
        verify(filterRepository).save(any(Filter.class));
    }

    @Test
    void testEditCompanyFilterFilterNotRelatedToCompany() {
        Long filterId = 1L;
        CompanyFilterEditDto dto = new CompanyFilterEditDto();
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(filterRepository.findById(filterId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.editCompanyFilter(filterId, dto, authentication));
    }

    @Test
    void testDeleteCompanyFilterSuccessfully() {
        Long filterId = 1L;
        CompanyFilterEditDto dto = new CompanyFilterEditDto();
        dto.setSkills(Set.of(1L, 2L));
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        Filter filter = new Filter();
        filter.setId(filterId);
        filter.setCompany(company);
        filter.setSkills(Set.of(new Skill(), new Skill()));
        company.setFilters(Set.of(filter));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(filterRepository.findById(filterId)).thenReturn(Optional.of(new Filter()));

        companyService.deleteCompanyFilter(filterId, authentication);

        verify(filterRepository).deleteById(filterId);
    }

    @Test
    void testDeleteCompanyFilterFilterNotRelatedToCompany() {
        Long filterId = 1L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(filterRepository.findById(filterId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.deleteCompanyFilter(filterId, authentication));
    }

    @Test
    void testAddCompanyFilterSuccessfully() {
        Long filterId = 1L;
        CompanyFilterEditDto dto = new CompanyFilterEditDto();
        dto.setSkills(Set.of(1L, 2L));
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        Filter filter = new Filter();
        filter.setId(filterId);
        filter.setCompany(company);
        filter.setName("Test Filter");
        filter.setSkills(new HashSet<>(Set.of(new Skill(), new Skill())));
        company.setFilters(new HashSet<>(Set.of(filter)));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(filterRepository.save(any(Filter.class))).thenReturn(filter);
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        CompanyFilterResponseDto response = companyService.addCompanyFilter(authentication, dto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(filterRepository).save(any(Filter.class));
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void testAddCompanyFilterInvalidCompany() {
        CompanyFilterRequestDto dto = new CompanyFilterRequestDto();
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.addCompanyFilter(authentication, dto));
    }

    @Test
    void testGetPartnerGroupsSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(1L);
        company.setPartnerGroups(Set.of(partnerGroup));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        List<PartnerGroupResponseDto> response = companyService.getPartnerGroups(authentication);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.getFirst().getId());
    }

    @Test
    void testDeletePartnerGroupSuccessfully() {
        Long partnerGroupId = 1L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(partnerGroupId);
        company.setPartnerGroups(new HashSet<>(Set.of(partnerGroup)));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(partnerGroupRepository.findById(partnerGroupId)).thenReturn(Optional.of(partnerGroup));

        companyService.deletePartnerGroup(authentication, partnerGroupId);

        verify(partnerGroupRepository).delete(partnerGroup);
    }

    @Test
    void testDeletePartnerGroupPartnerGroupNotFound() {
        Long partnerGroupId = 1L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(partnerGroupRepository.findById(partnerGroupId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.deletePartnerGroup(authentication, partnerGroupId));
    }

    @Test
    void testRemoveCompanyFromPartnersSuccessfully() {
        Long partnerGroupId = 1L;
        Long companyId = 2L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(partnerGroupId);
        Company companyToBeRemoved = new Company();
        companyToBeRemoved.setId(companyId);
        partnerGroup.setPartners(new HashSet<>(Set.of(companyToBeRemoved)));
        company.setPartnerGroups(new HashSet<>(Set.of(partnerGroup)));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(companyToBeRemoved));
        when(partnerGroupRepository.save(any(PartnerGroup.class))).thenReturn(partnerGroup);

        PartnerGroupResponseDto response = companyService.removeCompanyFromPartners(authentication, partnerGroupId, companyId);

        assertNotNull(response);
        assertEquals(partnerGroupId, response.getId());
        Assertions.assertFalse(response.getCompanies().stream().anyMatch(c -> c.getId().equals(companyId)));
        verify(partnerGroupRepository).save(any(PartnerGroup.class));
    }

    @Test
    void testRemoveCompanyFromPartnersPartnerGroupNotFound() {
        Long partnerGroupId = 1L;
        Long companyId = 2L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        company.setPartnerGroups(new HashSet<>());
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        assertThrows(IllegalArgumentException.class, () -> companyService.removeCompanyFromPartners(authentication, partnerGroupId, companyId));
    }

    @Test
    void testRemoveCompanyFromPartnersCompanyNotInPartnerGroup() {
        Long partnerGroupId = 1L;
        Long companyId = 2L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(partnerGroupId);
        Company companyToBeRemoved = new Company();
        companyToBeRemoved.setId(companyId);
        partnerGroup.setPartners(new HashSet<>());
        company.setPartnerGroups(new HashSet<>(Set.of(partnerGroup)));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(companyToBeRemoved));

        assertThrows(InvalidRequestException.class, () -> companyService.removeCompanyFromPartners(authentication, partnerGroupId, companyId));
    }

    @Test
    void testDeletePartnerGroupPermissionDenied() {
        Long partnerGroupId = 1L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        company.setPartnerGroups(new HashSet<>());
        currentUser.setCompany(company);
        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(partnerGroupId);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(partnerGroupRepository.findById(partnerGroupId)).thenReturn(Optional.of(partnerGroup));

        assertThrows(PermissionDeniedException.class, () -> companyService.deletePartnerGroup(authentication, partnerGroupId));
    }

    @Test
    void testGetAcceptedCompaniesPublicDataSuccessfully() {
        Company company1 = new Company();
        company1.setId(1L);
        Company company2 = new Company();
        company2.setId(2L);
        List<Company> verifiedCompanies = List.of(company1, company2);
        CompanyPublicResponseDto dto1 = new CompanyPublicResponseDto();
        dto1.setId(1L);
        CompanyPublicResponseDto dto2 = new CompanyPublicResponseDto();
        dto2.setId(2L);

        when(companyRepository.findCompaniesByEmailVerificationAccepted()).thenReturn(verifiedCompanies);
        when(imageService.returnUrlIfPictureExists(1L, "image")).thenReturn("image1.jpg");
        when(imageService.returnUrlIfPictureExists(2L, "image")).thenReturn("image2.jpg");

        List<CompanyPublicResponseDto> response = companyService.getAcceptedCompaniesPublicData();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("image1.jpg", response.get(0).getImage());
        assertEquals("image2.jpg", response.get(1).getImage());
    }

    @Test
    void testCreatePartnerGroupSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        PartnerGroupRequestDto dto = new PartnerGroupRequestDto();
        dto.setName("New Partner Group");
        dto.setCompanyIds(Set.of(2L, 3L));
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        company.setPartnerGroups(new HashSet<>());
        currentUser.setCompany(company);
        Company partnerCompany1 = new Company();
        partnerCompany1.setId(2L);
        Company partnerCompany2 = new Company();
        partnerCompany2.setId(3L);
        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(1L);
        partnerGroup.setName("New Partner Group");
        partnerGroup.setCompany(company);
        partnerGroup.setPartners(Set.of(partnerCompany1, partnerCompany2));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(2L)).thenReturn(Optional.of(partnerCompany1));
        when(companyRepository.findById(3L)).thenReturn(Optional.of(partnerCompany2));
        when(partnerGroupRepository.save(any(PartnerGroup.class))).thenReturn(partnerGroup);
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        PartnerGroupResponseDto response = companyService.createPartnerGroup(authentication, dto);

        assertNotNull(response);
        assertEquals("New Partner Group", response.getName());
        verify(partnerGroupRepository).save(any(PartnerGroup.class));
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void testCreatePartnerGroupAlreadyExists() {
        Authentication authentication = mock(Authentication.class);
        PartnerGroupRequestDto dto = new PartnerGroupRequestDto();
        dto.setName("Existing Partner Group");
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        PartnerGroup existingPartnerGroup = new PartnerGroup();
        existingPartnerGroup.setName("Existing Partner Group");
        company.setPartnerGroups(Set.of(existingPartnerGroup));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        assertThrows(AlreadyExistsException.class, () -> companyService.createPartnerGroup(authentication, dto));
    }

    @Test
    void testEditPartnerGroupSuccessfully() {
        Long partnerGroupId = 1L;
        PartnerGroupRequestDto dto = new PartnerGroupRequestDto();
        dto.setName("Updated Partner Group");
        dto.setCompanyIds(Set.of(2L, 3L));
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setId(partnerGroupId);
        partnerGroup.setCompany(company);
        Company partnerCompany1 = new Company();
        partnerCompany1.setId(2L);
        Company partnerCompany2 = new Company();
        partnerCompany2.setId(3L);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(partnerGroupRepository.findById(partnerGroupId)).thenReturn(Optional.of(partnerGroup));
        when(companyRepository.findById(2L)).thenReturn(Optional.of(partnerCompany1));
        when(companyRepository.findById(3L)).thenReturn(Optional.of(partnerCompany2));
        when(partnerGroupRepository.save(any(PartnerGroup.class))).thenReturn(partnerGroup);

        PartnerGroupResponseDto response = companyService.editPartnerGroup(authentication, partnerGroupId, dto);

        assertNotNull(response);
        assertEquals("Updated Partner Group", response.getName());
        verify(partnerGroupRepository).save(any(PartnerGroup.class));
    }

    @Test
    void testEditPartnerGroupPartnerGroupNotFound() {
        Long partnerGroupId = 1L;
        PartnerGroupRequestDto dto = new PartnerGroupRequestDto();
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(partnerGroupRepository.findById(partnerGroupId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.editPartnerGroup(authentication, partnerGroupId, dto));
    }

    @Test
    void testCreateTalentSuccessfully() {
        Long talentId = 1L;
        TalentRequestDto talentRequestDto = new TalentRequestDto();
        talentRequestDto.setLocations(List.of(1L, 2L));
        talentRequestDto.setWorkModes(List.of(1L, 2L));
        talentRequestDto.setMinRate(100);
        talentRequestDto.setMaxRate(200);
        TalentExperienceRequestDto experienceDto = new TalentExperienceRequestDto();
        SkillExperienceRequestDto skillExperienceRequestDto = new SkillExperienceRequestDto();
        skillExperienceRequestDto.setSkillId(1L);
        skillExperienceRequestDto.setMonths(5);
        experienceDto.setSkills(List.of(skillExperienceRequestDto));
        experienceDto.setPatternId(1L);
        experienceDto.setSeniorityId(1L);
        talentRequestDto.setExperience(experienceDto);
        Pattern pattern = new Pattern();
        pattern.setId(1L);
        Seniority seniority = new Seniority();
        seniority.setId(1L);

        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        company.setSkills(Set.of(new Skill()));
        currentUser.setCompany(company);
        Talent talent = new Talent();
        talent.setId(talentId);
        talent.setCompany(company);
        TalentExperience talentExperience = new TalentExperience();
        SkillExperience skillExperience = new SkillExperience();
        Skill skill = new Skill();
        skill.setId(1L);
        skill.setAssignable(true);
        skillExperience.setSkill(skill);
        talentExperience.setSkillExperienceList(List.of(skillExperience));
        talent.setTalentExperience(talentExperience);
        talentExperience.setSkillExperienceList(List.of(skillExperience));
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(seniority);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(talentRepository.findById(talentId)).thenReturn(Optional.of(talent));
        when(talentRepository.save(any(Talent.class))).thenReturn(talent);
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(new Location()));
        when(workModeRepository.findById(anyLong())).thenReturn(Optional.of(new WorkMode()));
        when(skillExperienceRepository.findById(anyLong())).thenReturn(Optional.of(new SkillExperience()));
        when(talentExperienceRepository.findById(1L)).thenReturn(Optional.of(new TalentExperience()));
        when(patternRepository.findById(1L)).thenReturn(Optional.of(pattern));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(seniorityRepository.findById(1L)).thenReturn(Optional.of(seniority));

        TalentResponseDto response = companyService.createTalent(authentication, talentRequestDto);

        assertNotNull(response);
        verify(talentRepository).save(any(Talent.class));
    }

    @Test
    void testCreateTalentInvalidSkills() {
        Authentication authentication = mock(Authentication.class);
        TalentRequestDto talentRequestDto = new TalentRequestDto();
        talentRequestDto.setExperience(new TalentExperienceRequestDto());

        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        doThrow(new InvalidRequestException("Invalid skills")).when(talentRepository).save(any(Talent.class));

        assertThrows(InvalidRequestException.class, () -> companyService.createTalent(authentication, talentRequestDto));
    }

    @Test
    void testUpdateTalentSuccessfully() {
        Long talentId = 1L;
        TalentRequestDto talentRequestDto = new TalentRequestDto();
        talentRequestDto.setLocations(List.of(1L, 2L));
        talentRequestDto.setWorkModes(List.of(1L, 2L));
        talentRequestDto.setMinRate(100);
        talentRequestDto.setMaxRate(200);
        TalentExperienceRequestDto experienceDto = new TalentExperienceRequestDto();
        SkillExperienceRequestDto skillExperienceRequestDto = new SkillExperienceRequestDto();
        skillExperienceRequestDto.setSkillId(1L);
        skillExperienceRequestDto.setMonths(5);
        experienceDto.setSkills(List.of(skillExperienceRequestDto));
        experienceDto.setPatternId(1L);
        experienceDto.setSeniorityId(1L);
        talentRequestDto.setExperience(experienceDto);
        Pattern pattern = new Pattern();
        pattern.setId(1L);
        Seniority seniority = new Seniority();
        seniority.setId(1L);

        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        company.setSkills(Set.of(new Skill()));
        currentUser.setCompany(company);
        Talent talent = new Talent();
        talent.setId(talentId);
        talent.setCompany(company);
        TalentExperience talentExperience = new TalentExperience();
        SkillExperience skillExperience = new SkillExperience();
        Skill skill = new Skill();
        skill.setId(1L);
        skill.setAssignable(true);
        skillExperience.setSkill(skill);
        talentExperience.setSkillExperienceList(List.of(skillExperience));
        talent.setTalentExperience(talentExperience);
        talentExperience.setSkillExperienceList(List.of(skillExperience));
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(seniority);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(talentRepository.findById(talentId)).thenReturn(Optional.of(talent));
        when(talentRepository.save(any(Talent.class))).thenReturn(talent);
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(new Location()));
        when(workModeRepository.findById(anyLong())).thenReturn(Optional.of(new WorkMode()));
        when(skillExperienceRepository.findById(anyLong())).thenReturn(Optional.of(new SkillExperience()));
        when(talentExperienceRepository.findById(1L)).thenReturn(Optional.of(new TalentExperience()));
        when(patternRepository.findById(1L)).thenReturn(Optional.of(pattern));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(seniorityRepository.findById(1L)).thenReturn(Optional.of(seniority));

        TalentResponseDto response = companyService.updateTalent(authentication, talentId, talentRequestDto);

        assertNotNull(response);
        assertEquals(talentId, response.getId());
        verify(talentRepository, times(2)).save(any(Talent.class));
    }

    @Test
    void testUpdateTalentTalentNotFound() {
        Long talentId = 1L;
        TalentRequestDto talentRequestDto = new TalentRequestDto();

        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(talentRepository.findById(talentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.updateTalent(authentication, talentId, talentRequestDto));
    }

    @Test
    void testGetTalentByIdTalentNotFound() {
        Long talentId = 1L;
        Authentication authentication = mock(Authentication.class);

        when(talentRepository.findById(talentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.getTalentById(authentication, talentId));
    }

    @Test
    void testGetMyTalentsSuccessfully() {
        Long talentId = 1L;
        TalentRequestDto talentRequestDto = new TalentRequestDto();
        talentRequestDto.setLocations(List.of(1L, 2L));
        talentRequestDto.setWorkModes(List.of(1L, 2L));
        talentRequestDto.setMinRate(100);
        talentRequestDto.setMaxRate(200);
        TalentExperienceRequestDto experienceDto = new TalentExperienceRequestDto();
        SkillExperienceRequestDto skillExperienceRequestDto = new SkillExperienceRequestDto();
        skillExperienceRequestDto.setSkillId(1L);
        skillExperienceRequestDto.setMonths(5);
        experienceDto.setSkills(List.of(skillExperienceRequestDto));
        experienceDto.setPatternId(1L);
        experienceDto.setSeniorityId(1L);
        talentRequestDto.setExperience(experienceDto);
        Pattern pattern = new Pattern();
        pattern.setId(1L);
        Seniority seniority = new Seniority();
        seniority.setId(1L);

        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        company.setSkills(Set.of(new Skill()));
        currentUser.setCompany(company);
        Talent talent = new Talent();
        talent.setId(talentId);
        talent.setCompany(company);
        TalentExperience talentExperience = new TalentExperience();
        SkillExperience skillExperience = new SkillExperience();
        Skill skill = new Skill();
        skill.setId(1L);
        skill.setAssignable(true);
        skillExperience.setSkill(skill);
        talentExperience.setSkillExperienceList(List.of(skillExperience));
        talent.setTalentExperience(talentExperience);
        talentExperience.setSkillExperienceList(List.of(skillExperience));
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(seniority);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(talentRepository.findById(talentId)).thenReturn(Optional.of(talent));
        when(talentRepository.save(any(Talent.class))).thenReturn(talent);
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(new Location()));
        when(workModeRepository.findById(anyLong())).thenReturn(Optional.of(new WorkMode()));
        when(skillExperienceRepository.findById(anyLong())).thenReturn(Optional.of(new SkillExperience()));
        when(talentExperienceRepository.findById(1L)).thenReturn(Optional.of(new TalentExperience()));
        when(patternRepository.findById(1L)).thenReturn(Optional.of(pattern));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(seniorityRepository.findById(1L)).thenReturn(Optional.of(seniority));

        List<TalentResponseDto> response = companyService.getMyTalents(authentication);

        assertNotNull(response);
        verify(talentRepository).findByCompanyId(company.getId());
    }

    @Test
    void testDeleteTalentSuccessfully() {
        Long talentId = 1L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        Talent talent = new Talent();
        talent.setId(talentId);
        talent.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(talentRepository.findById(talentId)).thenReturn(Optional.of(talent));

        companyService.deleteTalent(authentication, talentId);

        verify(talentRepository).delete(talent);
    }

    @Test
    void testDeleteTalentTalentNotFound() {
        Long talentId = 1L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(talentRepository.findById(talentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.deleteTalent(authentication, talentId));
    }

    @Test
    void testSetTalentVisibilityPublic() {
        Authentication authentication = mock(Authentication.class);
        TalentPublicityRequestDto requestDto = new TalentPublicityRequestDto();
        requestDto.setPublic(true);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        companyService.setTalentVisibility(authentication, requestDto);

        assertTrue(company.isTalentsSharedPublicly());
        assertTrue(company.getTalentAccessGroups().isEmpty());
        verify(companyRepository).save(company);
    }

    @Test
    void testSetTalentVisibilityPrivate() {
        Authentication authentication = mock(Authentication.class);
        TalentPublicityRequestDto requestDto = new TalentPublicityRequestDto();
        requestDto.setPublic(false);
        requestDto.setPartnerGroupIds(Set.of(1L, 2L));
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);
        PartnerGroup partnerGroup1 = new PartnerGroup();
        partnerGroup1.setId(1L);
        partnerGroup1.setCompany(company);
        PartnerGroup partnerGroup2 = new PartnerGroup();
        partnerGroup2.setId(2L);
        partnerGroup2.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(partnerGroupRepository.findById(1L)).thenReturn(Optional.of(partnerGroup1));
        when(partnerGroupRepository.findById(2L)).thenReturn(Optional.of(partnerGroup2));

        companyService.setTalentVisibility(authentication, requestDto);

        Assertions.assertFalse(company.isTalentsSharedPublicly());
        assertEquals(2, company.getTalentAccessGroups().size());
        verify(companyRepository).save(company);
    }

    @Test
    void testSetTalentVisibilityPermissionDenied() {
        Authentication authentication = mock(Authentication.class);
        TalentPublicityRequestDto requestDto = new TalentPublicityRequestDto();
        requestDto.setPublic(false);
        requestDto.setPartnerGroupIds(Set.of(1L));

        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        currentUser.setCompany(company);

        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setCompany(new Company());

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(partnerGroupRepository.findById(1L)).thenReturn(Optional.of(partnerGroup));

        assertThrows(PermissionDeniedException.class, () -> companyService.setTalentVisibility(authentication, requestDto));
    }

    @Test
    void testGetTalentVisibilityPublicTalents() {
        Authentication authentication = mock(Authentication.class);
        User user = new User();
        Company company = new Company();
        company.setTalentsSharedPublicly(true);
        user.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);

        TalentPublicityResponseDto result = companyService.getTalentVisibility(authentication);

        assertTrue(result.isPublic());
        assertTrue(result.getPartnerGroupIds().isEmpty());
    }

    @Test
    void testGetTalentVisibilityPrivateTalents() {
        Authentication authentication = mock(Authentication.class);
        User user = new User();
        Company company = new Company();
        company.setTalentsSharedPublicly(false);

        PartnerGroup pg1 = new PartnerGroup();
        pg1.setId(1L);
        PartnerGroup pg2 = new PartnerGroup();
        pg2.setId(2L);

        company.setTalentAccessGroups(Set.of(pg1, pg2));
        user.setCompany(company);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);

        TalentPublicityResponseDto result = companyService.getTalentVisibility(authentication);

        Assertions.assertFalse(result.isPublic());
        assertEquals(Set.of(1L, 2L), result.getPartnerGroupIds());
    }

    @Test
    void testValidateTalentIsAvailableToCompany_TalentBelongsToTheCompany() {
        Company company = new Company();
        company.setId(1L);

        Talent talent = new Talent();
        talent.setId(1L);
        talent.setCompany(company);
        talent.setActive(true);
        talent.setWorkModes(Set.of(new WorkMode()));
        talent.setLocations(Set.of(new Location()));
        talent.setMaxRate(1);
        talent.setMaxRate(2);
        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setPattern(new Pattern());
        talentExperience.setSeniority(new Seniority());
        talentExperience.setTotalTime(1);
        talent.setTalentExperience(talentExperience);

        User user = new User();
        user.setCompany(company);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(talentRepository.findById(1L)).thenReturn(Optional.of(talent));

        TalentResponseDto result = companyService.getTalentById(authentication, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testValidateTalentIsAvailableToCompany_TalentSharedWithTheCompany() {
        Company company = new Company();
        company.setId(1L);

        Company talentCompany = new Company();
        talentCompany.setId(2L);

        PartnerGroup partnerGroup = new PartnerGroup();
        partnerGroup.setPartners(Set.of(company));

        talentCompany.setPartnerGroups(Set.of(partnerGroup));

        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setPattern(new Pattern());
        talentExperience.setSeniority(new Seniority());
        talentExperience.setTotalTime(15);

        Talent talent = new Talent();
        talent.setId(1L);
        talent.setCompany(talentCompany);
        talent.setActive(true);
        talent.setWorkModes(Set.of(new WorkMode()));
        talent.setLocations(Set.of(new Location()));
        talent.setMinRate(3);
        talent.setMaxRate(15);
        talent.setTalentExperience(talentExperience);

        User user = new User();
        user.setCompany(company);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(talentRepository.findById(1L)).thenReturn(Optional.of(talent));

        TalentResponseDto result = companyService.getTalentById(authentication, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /*@Test
    void shouldGetAllTalents() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        userCompany.setTalentsSharedPublicly(true);
        user.setCompany(userCompany);

        WorkMode wm = WorkMode.builder()
                .id(1L).build();
        Location loc = Location.builder()
                .id(1L).build();
        Pattern pattern = new Pattern();
        pattern.setId(1L);
        Seniority seniority = Seniority.builder().id(1L).build();

        TalentExperience te = TalentExperience.builder()
                .id(1L)
                .pattern(pattern)
                .seniority(seniority)
                .build();

        Talent talent = Talent.builder()
                .id(1L)
                .workModes(Set.of(wm))
                .locations(Set.of(loc))
                .talentExperience(te)
                .isActive(true)
                .company(userCompany)
                .build();

        te.setTalent(talent);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Talent> talentPage = new PageImpl<>(List.of(talent), pageable, 1);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(talentRepository.findAllActiveTalentsVisibleToCompany(anyLong(), any(Pageable.class))).thenReturn(talentPage);

        Page<TalentResponseDto> resultPage = companyService.getAllTalents(authentication, pageable);

        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());
        assertEquals(talent.getId(), resultPage.getContent().get(0).getId());
    }*/

    @Test
    void shouldThrowExceptionWhenGetTalentByNonExistingId() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        when(talentRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> companyService.getTalentById(authentication, 1L));

        assertEquals(exception.getMessage(), "Talent with ID: " + 1L + " not found");
    }

    @Test
    void shouldThrowPermissionDeniedExceptionWhenGetTalentByIdIsNotAccessible() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        Company thirdCompany = Company.builder()
                .id(3L)
                .build();

        PartnerGroup pg = PartnerGroup.builder()
                .partners(Set.of(thirdCompany))
                .build();

        Company anotherCompany = Company.builder().id(2L).build();
        anotherCompany.setTalentsSharedPublicly(false);
        anotherCompany.setPartnerGroups(Set.of(pg));

        Talent t = Talent.builder().id(1L).isActive(true).company(anotherCompany).build();

        when(talentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);

        PermissionDeniedException exception = assertThrows(PermissionDeniedException.class, () -> companyService.getTalentById(authentication, 1L));

        assertEquals(exception.getMessage(), "You have no access to this talent");
    }

    @Test
    void shouldThrowPermissionDeniedExceptionWhenGetTalentByIdIsNotActive() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        Company thirdCompany = Company.builder()
                .id(3L)
                .build();

        PartnerGroup pg = PartnerGroup.builder()
                .partners(Set.of(thirdCompany))
                .build();

        Company anotherCompany = Company.builder().id(2L).build();
        anotherCompany.setTalentsSharedPublicly(false);
        anotherCompany.setPartnerGroups(Set.of(pg));

        Talent t = Talent.builder().id(1L).isActive(false).company(anotherCompany).build();

        when(talentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);

        PermissionDeniedException exception = assertThrows(PermissionDeniedException.class, () -> companyService.getTalentById(authentication, 1L));

        assertEquals(exception.getMessage(), "Talent is not active.");
    }

    @Test
    void shouldThrowPermissionDeniedExceptionWhenGetTalentByIdIsInactive() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        Company anotherCompany = Company.builder().id(2L).build();
        anotherCompany.setTalentsSharedPublicly(true);

        Talent t = Talent.builder().id(1L).isActive(false).company(anotherCompany).build();

        when(talentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);

        PermissionDeniedException exception = assertThrows(PermissionDeniedException.class, () -> companyService.getTalentById(authentication, 1L));

        assertEquals(exception.getMessage(), "Talent is inactive");
    }

    @Test
    void updateCompanyWithOtherFieldsIncluded(){
        User user = User.builder().id(1L).build();

        Company company = Company.builder()
                .name("currentName")
                .email("email@test.test")
                .website("test.com")
                .linkedIn("linkedIn.com")
                .emailVerification(EmailVerification.ACCEPTED)
                .build();

        user.setCompany(company);

        Project project = new Project();
        project.setCompany(company);

        Position position = new Position();
        position.setId(1L);
        position.setViews(2L);
        position.setProject(project);

        project.setPositions(List.of(position));
        company.setProjects(List.of(project));

        PositionApplication pa = new PositionApplication();
        pa.setPosition(position);
        pa.setApplicationStatus(ApplicationStatus.ACCEPTED);
        PositionApplication pa2 = new PositionApplication();
        pa2.setPosition(position);
        pa2.setApplicationStatus(ApplicationStatus.IN_PROGRESS);

        CompanyType ct = CompanyType.builder().id(1L).build();
        company.setCompanyType(ct);
        CompanyType newCompanyType = CompanyType.builder().id(2L).build();

        Domain domain = Domain.builder().id(1L).build();
        company.setDomain(domain);
        Domain newDomain = Domain.builder().id(2L).build();

        Skill currentSkill = Skill.builder().id(1L).build();
        company.setSkills(Set.of(currentSkill));

        Skill newSkill = Skill.builder().id(2L).build();
        company.setDescription("oldDescription");

        CompanyRequestDto dto = new CompanyRequestDto();
        dto.setName("newName");
        dto.setEmail("email@test.test");
        dto.setWebsite("test.com");
        dto.setLinkedIn("linkedin.com");
        dto.setCompanyTypeId(2L);
        dto.setSkills(List.of(2L));
        dto.setDescription("newDescription");
        dto.setDomainId(2L);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyRepository.findByLinkedIn(any())).thenReturn(Optional.empty());
        when(companyRepository.findByWebsite(any())).thenReturn(Optional.empty());
        when(companyTypeRepository.findById(anyLong())).thenReturn(Optional.of(newCompanyType));
        when(domainRepository.findById(anyLong())).thenReturn(Optional.of(newDomain));
        when(patternService.getAllSkillsIfSkillIdsExist(any())).thenReturn(List.of(newSkill));
        when(positionApplicationRepository.countByPositionIdExcludingAwaitingCvOrTalent(anyLong())).thenReturn(2L);
        when(positionApplicationRepository.countByPositionIdAndApplicationStatus(anyLong(), any())).thenReturn(1L);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompanyResponseDto responseDto = companyService.editCompany(authentication, dto, request);

        assertEquals(responseDto.getName(), "newName");
        assertEquals(responseDto.getPositionViews(), 2L);
        assertEquals(responseDto.getAcceptedApplications(), 1L);
        assertEquals(responseDto.getTotalApplications(), 2L);
    }

    @Test
    void shouldReturnTalentsBasedOnMatchingWorkModes() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        currentUser.setCompany(userCompany);

        WorkMode workMode = new WorkMode();
        workMode.setName("workmoder");
        workMode.setId(1L);

        Talent talent = new Talent();
        talent.setWorkModes(Set.of(workMode));
        talent.setCompany(userCompany);

        Location location = new Location();
        location.setId(1L);
        location.setName("testLocation");

        Pattern pattern = new Pattern();
        pattern.setId(1L);
        pattern.setName("testPattern");

        talent.setLocations(Set.of(location));
        talent.setWorkModes(Set.of(workMode));
        talent.setActive(true);
        talent.setMinRate(10);
        talent.setMaxRate(20);

        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setId(1L);
        talentExperience.setTalent(talent);
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(new Seniority());

        talent.setTalentExperience(talentExperience);


        Page<Talent> talentPage = new PageImpl<>(List.of(talent));
        when(talentRepository.findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), any(Pageable.class)))
                .thenReturn(talentPage);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        List<Long> workModesIds = List.of(1L);
        List<Long> skillsIds = new ArrayList<>();
        Integer rate = 50;

        Page<TalentResponseDto> result = companyService.getAllTalents(authentication, 0, 5, "minRate", true, workModesIds, skillsIds, rate);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().getFirst().getWorkModes().contains(1L));
    }

    @Test
    void shouldReturnEmptyPageWhenNoTalentsMatchFilters() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        currentUser.setCompany(userCompany);

        WorkMode workMode = new WorkMode();
        workMode.setName("workmoder");
        workMode.setId(1L);

        Talent talent = new Talent();
        talent.setWorkModes(Set.of(workMode));
        talent.setCompany(userCompany);

        Location location = new Location();
        location.setId(1L);
        location.setName("testLocation");

        Pattern pattern = new Pattern();
        pattern.setId(1L);
        pattern.setName("testPattern");

        talent.setLocations(Set.of(location));
        talent.setWorkModes(Set.of(workMode));
        talent.setActive(true);
        talent.setMinRate(10);
        talent.setMaxRate(20);

        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setId(1L);
        talentExperience.setTalent(talent);
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(new Seniority());

        talent.setTalentExperience(talentExperience);

        Page<Talent> talentPage = Page.empty();
        when(talentRepository.findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), any(Pageable.class)))
                .thenReturn(talentPage);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        List<Long> workModesIds = List.of(100L);
        List<Long> skillsIds = List.of(200L);
        Integer rate = 50;

        Page<TalentResponseDto> result = companyService.getAllTalents(authentication, 0, 5, "minRate", true, workModesIds, skillsIds, rate);

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(talentRepository).findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), any(Pageable.class));
    }

    @Test
    void shouldReturnTalentsBasedOnMatchingSkills() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        currentUser.setCompany(userCompany);

        WorkMode workMode = new WorkMode();
        workMode.setName("workmoder");
        workMode.setId(1L);

        Talent talent = new Talent();
        talent.setWorkModes(Set.of(workMode));
        talent.setCompany(userCompany);

        Location location = new Location();
        location.setId(1L);
        location.setName("testLocation");

        Pattern pattern = new Pattern();
        pattern.setId(1L);
        pattern.setName("testPattern");

        talent.setLocations(Set.of(location));
        talent.setWorkModes(Set.of(workMode));
        talent.setActive(true);
        talent.setMinRate(10);
        talent.setMaxRate(20);

        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setId(1L);
        talentExperience.setTalent(talent);
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(new Seniority());

        talent.setTalentExperience(talentExperience);

        Page<Talent> talentPage = new PageImpl<>(List.of(talent));
        when(talentRepository.findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), any(Pageable.class)))
                .thenReturn(talentPage);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        List<Long> workModesIds = new ArrayList<>();
        List<Long> skillsIds = List.of(1L);
        Integer rate = 50;

        Page<TalentResponseDto> result = companyService.getAllTalents(authentication, 0, 5, "minRate", true, workModesIds, skillsIds, rate);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().getFirst().getWorkModes().contains(1L));
    }

    @Test
    void shouldReturnTalentsBasedOnMatchingRate() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        currentUser.setCompany(userCompany);

        WorkMode workMode = new WorkMode();
        workMode.setName("workmoder");
        workMode.setId(1L);

        Talent talent = new Talent();
        talent.setWorkModes(Set.of(workMode));
        talent.setCompany(userCompany);

        Location location = new Location();
        location.setId(1L);
        location.setName("testLocation");

        Pattern pattern = new Pattern();
        pattern.setId(1L);
        pattern.setName("testPattern");

        talent.setLocations(Set.of(location));
        talent.setWorkModes(Set.of(workMode));
        talent.setActive(true);
        talent.setMinRate(10);
        talent.setMaxRate(20);

        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setId(1L);
        talentExperience.setTalent(talent);
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(new Seniority());

        talent.setTalentExperience(talentExperience);

        Page<Talent> talentPage = new PageImpl<>(List.of(talent));
        when(talentRepository.findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), any(Pageable.class)))
                .thenReturn(talentPage);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        List<Long> workModesIds = new ArrayList<>();
        List<Long> skillsIds = new ArrayList<>();
        Integer rate = 15;

        Page<TalentResponseDto> result = companyService.getAllTalents(authentication, 0, 5, "minRate", true, workModesIds, skillsIds, rate);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().getFirst().getWorkModes().contains(1L));
    }

    @Test
    void shouldReturnPaginatedTalents() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        currentUser.setCompany(userCompany);

        WorkMode workMode = new WorkMode();
        workMode.setName("workmoder");
        workMode.setId(1L);

        Talent talent = new Talent();
        talent.setWorkModes(Set.of(workMode));
        talent.setCompany(userCompany);

        Location location = new Location();
        location.setId(1L);
        location.setName("testLocation");

        Pattern pattern = new Pattern();
        pattern.setId(1L);
        pattern.setName("testPattern");

        talent.setLocations(Set.of(location));
        talent.setWorkModes(Set.of(workMode));
        talent.setActive(true);
        talent.setMinRate(10);
        talent.setMaxRate(20);

        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setId(1L);
        talentExperience.setTalent(talent);
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(new Seniority());

        talent.setTalentExperience(talentExperience);

        Page<Talent> talentPage = new PageImpl<>(List.of(talent));
        when(talentRepository.findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), any(Pageable.class)))
                .thenReturn(talentPage);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        List<Long> workModesIds = List.of(1L);
        List<Long> skillsIds = new ArrayList<>();
        Integer rate = 50;

        Page<TalentResponseDto> result = companyService.getAllTalents(authentication, 0, 5, "minRate", true, workModesIds, skillsIds, rate);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().getFirst().getWorkModes().contains(1L));
    }

    @Test
    void shouldSetDefaultPageSizeWhenPageSizeIsZero() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        currentUser.setCompany(userCompany);

        WorkMode workMode = new WorkMode();
        workMode.setName("workmoder");
        workMode.setId(1L);

        Talent talent = new Talent();
        talent.setWorkModes(Set.of(workMode));
        talent.setCompany(userCompany);

        Location location = new Location();
        location.setId(1L);
        location.setName("testLocation");

        Pattern pattern = new Pattern();
        pattern.setId(1L);
        pattern.setName("testPattern");

        talent.setLocations(Set.of(location));
        talent.setWorkModes(Set.of(workMode));
        talent.setActive(true);
        talent.setMinRate(10);
        talent.setMaxRate(20);

        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setId(1L);
        talentExperience.setTalent(talent);
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(new Seniority());

        talent.setTalentExperience(talentExperience);

        Page<Talent> talentPage = new PageImpl<>(List.of(talent));
        when(talentRepository.findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), any(Pageable.class)))
                .thenReturn(talentPage);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        List<Long> workModesIds = List.of(1L);
        List<Long> skillsIds = new ArrayList<>();
        Integer rate = 50;

        // With pageSize = 0 (should be default to 5)
        Page<TalentResponseDto> result = companyService.getAllTalents(authentication, 0, 0, "minRate", true, workModesIds, skillsIds, rate);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getWorkModes().contains(1L));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(talentRepository).findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(5, pageable.getPageSize());
    }

    @Test
    void shouldSetDefaultPageSizeWhenPageSizeIsZeroOrNegative() {
        User user = new User();
        user.setId(1L);
        Company userCompany = Company.builder().id(1L).build();
        userCompany.setId(1L);
        user.setCompany(userCompany);

        currentUser.setCompany(userCompany);

        WorkMode workMode = new WorkMode();
        workMode.setName("workmoder");
        workMode.setId(1L);

        Talent talent = new Talent();
        talent.setWorkModes(Set.of(workMode));
        talent.setCompany(userCompany);

        Location location = new Location();
        location.setId(1L);
        location.setName("testLocation");

        Pattern pattern = new Pattern();
        pattern.setId(1L);
        pattern.setName("testPattern");

        talent.setLocations(Set.of(location));
        talent.setWorkModes(Set.of(workMode));
        talent.setActive(true);
        talent.setMinRate(10);
        talent.setMaxRate(20);

        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setId(1L);
        talentExperience.setTalent(talent);
        talentExperience.setPattern(pattern);
        talentExperience.setSeniority(new Seniority());

        talent.setTalentExperience(talentExperience);

        Page<Talent> talentPage = new PageImpl<>(List.of(talent));
        when(talentRepository.findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), any(Pageable.class)))
                .thenReturn(talentPage);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        List<Long> workModesIds = List.of(1L);
        List<Long> skillsIds = new ArrayList<>();
        Integer rate = 50;

        // With pageSize negative (should be default to 5)
        Page<TalentResponseDto> result = companyService.getAllTalents(authentication, 0, -10, "minRate", true, workModesIds, skillsIds, rate);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getWorkModes().contains(1L));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(talentRepository).findAllActiveTalentsVisibleToCompany(eq(userCompany.getId()), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(5, pageable.getPageSize());
    }
}
