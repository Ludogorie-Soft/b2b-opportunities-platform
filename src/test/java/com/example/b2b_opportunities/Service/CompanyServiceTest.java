package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyFilterEditDto;
import com.example.b2b_opportunities.Dto.Request.CompanyFilterRequestDto;
import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Request.PartnerGroupRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyFilterResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyPublicResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Dto.Response.PartnerGroupResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.Filter;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
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
import com.example.b2b_opportunities.Repository.PartnerGroupRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.Static.ProjectStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompanyServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private MailService mailService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatternService patternService;

    @Mock
    private FilterRepository filterRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private CompanyTypeRepository companyTypeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private PartnerGroupRepository partnerGroupRepository;

    @InjectMocks
    private CompanyService companyService;

    private CompanyRequestDto companyRequestDto;

    private User currentUser;

    @BeforeEach
    public void setUp() {
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
    public void testCreateCompanyThrowsAuthenticationFailedException() {
        Authentication authentication = null;

        when(userService.getCurrentUserOrThrow(authentication))
                .thenThrow(new AuthenticationFailedException("User not authenticated"));

        assertThrows(AuthenticationFailedException.class, () -> companyService.createCompany(authentication, companyRequestDto, request));
    }

    @Test
    public void shouldThrowInvalidRequestExceptionWhenUserAlreadyHasCompany() {
        Company existingCompany = new Company();
        existingCompany.setName("Existing Company");
        currentUser.setCompany(existingCompany);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);

        assertThrows(InvalidRequestException.class, () -> companyService.createCompany(authentication, companyRequestDto, request));
    }

    @Test
    public void ShouldCreateCompanyWhenValidRequest() {
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
    public void shouldGetCompanyAndUsersSuccessfully() {
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
    public void shouldThrowNotFoundExceptionWhenCompanyDoesNotExist() {
        Long companyId = 999L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> companyService.getCompanyAndUsers(companyId));

        assertEquals("Company with ID: " + companyId + " not found", thrown.getMessage());
        verify(companyRepository).findById(companyId);
    }

    @Test
    public void shouldReturnEmptyListWhenCompanyHasNoUsers() {
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
    public void shouldReturnListOfUsersWhenCompanyHasUsers() {
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
    public void shouldConfirmCompanyWithValidToken() {
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
    public void shouldNotConfirmCompanyEmailWithInvalidToken() {
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
    public void shouldEditCompanySuccessfully() {
        MultipartFile image = mock(MultipartFile.class);
        MultipartFile banner = mock(MultipartFile.class);

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
    public void shouldThrowNotFoundExceptionWhenUserHasNoCompany() {
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
        assertEquals(1L, response.get(0).getId());
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
        company.setPartnerGroups(new HashSet<>()); // Initialize the partnerGroups set
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
    void testDeletePartnerGroupSuccess() {
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
    void testDeletePartnerGroupPermissionDenied() {
        Long partnerGroupId = 1L;
        Authentication authentication = mock(Authentication.class);
        User currentUser = new User();
        Company company = new Company();
        company.setId(1L);
        company.setPartnerGroups(new HashSet<>()); // Initialize the partnerGroups set
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
    void testEditPartnerGroupSuccess() {
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
}