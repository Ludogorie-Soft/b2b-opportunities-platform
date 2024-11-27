package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Repository.DomainRepository;
import com.example.b2b_opportunities.Repository.FilterRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import jakarta.servlet.http.HttpServletRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    private CompanyTypeRepository companyTypeRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest request;

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
    public void testCreateCompany_ThrowsAuthenticationFailedException() {
        Authentication authentication = null;

        when(userService.getCurrentUserOrThrow(authentication))
                .thenThrow(new AuthenticationFailedException("User not authenticated"));

        assertThrows(AuthenticationFailedException.class, () -> {
            companyService.createCompany(authentication, companyRequestDto, request);
        });
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

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            companyService.getCompanyAndUsers(companyId);
        });

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
        // Arrange
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
        assertEquals("New Company Name", responseDto.getName()); // Validate name is updated
        assertEquals("newemail@example.com", userCompany.getEmail()); // Verify that the email has been updated
        assertEquals("newwebsite.com", userCompany.getWebsite()); // Verify that the website has been updated
        verify(companyRepository, times(2)).save(userCompany);
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenUserHasNoCompany() {
        User currentUser = new User();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(currentUser);
        when(companyRepository.findById(currentUser.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            companyService.editCompany(authentication, companyRequestDto, request);
        });

        verify(companyRepository, never()).save(any());
    }
}