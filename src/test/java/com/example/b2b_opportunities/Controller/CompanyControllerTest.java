package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Repository.DomainRepository;
import com.example.b2b_opportunities.Repository.RoleRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Service.ImageService;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = true)
@Testcontainers
class CompanyControllerTest {
    private static final String HOST_PATH = Paths.get("Deploy/icons").toAbsolutePath().toString();
    private static final String CONTAINER_PATH = "/icons";

    @Container
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withExposedPorts(5432)
            .withFileSystemBind(HOST_PATH, CONTAINER_PATH, BindMode.READ_ONLY);

    @DynamicPropertySource
    private static void overridePropertiesFile(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CompanyTypeRepository companyTypeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DomainRepository domainRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ImageService imageService;
    private Company company1;
    private Company company2;
    private User user;
    private Authentication authentication;
    private CompanyRequestDto companyRequestDto;

    @BeforeEach
    void init() {
        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@abv.bgg")
                .username("testUser")
                .isApproved(true)
                .isEnabled(true)
                .password("password")
                .role(roleRepository.findById(2L).orElseThrow())
                .build();

        userRepository.save(user);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        CompanyType ct = CompanyType.builder().name("test_ct").build();
        ct = companyTypeRepository.save(ct);

        companyRepository.deleteAll();

        company1 = Company.builder()
                .name("Company A")
                .email("company_a@abvz.com")
                .companyType(ct)
                .skills(new HashSet<>())
                .emailVerification(EmailVerification.ACCEPTED)
                .build();

        company2 = Company.builder()
                .name("Company B")
                .email("company_b@abvz.com")
                .companyType(ct)
                .skills(new HashSet<>())
                .emailVerification(EmailVerification.ACCEPTED)
                .build();

        companyRepository.save(company1);
        companyRepository.save(company2);
        companyRepository.flush();

        Domain domain = Domain.builder().name("test_dm").build();

        domain = domainRepository.save(domain);

        companyRequestDto = new CompanyRequestDto();
        companyRequestDto.setName("New Company");
        companyRequestDto.setEmail("johndoe@abv.bgg");
        companyRequestDto.setCompanyTypeId(ct.getId());
        companyRequestDto.setSkills(new ArrayList<>());
        companyRequestDto.setDomainId(domain.getId());
        companyRequestDto.setWebsite("http://test.com");
        companyRequestDto.setLinkedIn("http://test.com");
        companyRequestDto.setDescription("test test");
    }

    @Test
    void getCompaniesShouldReturnListOfCompanies() throws Exception {
        mockMvc.perform(get("/companies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // Check that 2 companies are returned
                .andExpect(jsonPath("$[0].name").value("Company A"))
                .andExpect(jsonPath("$[1].name").value("Company B"));
    }

    @Test
    void getCompaniesShouldReturnEmptyListWhenNoCompaniesExist() throws Exception {
        companyRepository.deleteAll();

        mockMvc.perform(get("/companies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getCompanyByIdShouldReturnCompanyWhenExists() throws Exception {
        mockMvc.perform(get("/companies/" + company1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Company A"))
                .andExpect(jsonPath("$.email").value("company_a@abvz.com"));
    }

    @Test
    void getCompanyByIdShouldThrowExceptionWhenCompanyDoesNotExist() throws Exception {
        mockMvc.perform(get("/companies/9999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Company with ID: 9999999 not found"));
    }

    @Test
    void createCompanyShouldReturnCreatedWhenValidRequest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(imageService.returnUrlIfPictureExists(anyLong(), eq("image"))).thenReturn(null);
        when(imageService.returnUrlIfPictureExists(anyLong(), eq("banner"))).thenReturn(null);

        String companyRequestDtoJson = objectMapper.writeValueAsString(companyRequestDto);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyRequestDtoJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Company"))
                .andExpect(jsonPath("$.email").value("johndoe@abv.bgg"))
                .andExpect(jsonPath("$.website").value("http://test.com"))
                .andExpect(jsonPath("$.linkedIn").value("http://test.com"))
                .andExpect(jsonPath("$.description").value("test test"));
    }

    //TODO -> Add test to create company with different than user's email -> Email verification should be sent

    @Test
    void shouldGetCompanyAndUsers() throws Exception {
        User companyUser = new User();
        companyUser.setUsername("company_user");
        companyUser.setEmail("testmail@test.test");
        companyUser.setFirstName("first_name");
        companyUser.setLastName("last_name");
        companyUser.setCompany(company1);
        userRepository.save(companyUser);

        company1 = companyRepository.findById(company1.getId()).orElseThrow();
        company1.setUsers(List.of(companyUser));

        mockMvc.perform(get("/companies/" + company1.getId() + "/with-users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.company.name").value("Company A"))
                .andExpect(jsonPath("$.company.email").value("company_a@abvz.com"))
                .andExpect(jsonPath("$.users[*].username").value("company_user"));
    }

    @Test
    void shouldThrowExceptionForNonExistingCompany() throws Exception {
        mockMvc.perform(get("/companies/" + 99999999 + "/with-users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Company with ID: 99999999 not found"));
    }

    @Test
    void shouldThrowExceptionForExistingCompanyName() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        companyRequestDto.setName("Company A"); // existing company name
        String companyRequestDtoJson = objectMapper.writeValueAsString(companyRequestDto);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyRequestDtoJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Name already registered"));
    }

    @Test
    void shouldConfirmEmailAndRedirect() throws Exception {
        company1.setEmailVerification(EmailVerification.PENDING);
        company1.setEmailConfirmationToken("test-token");

        mockMvc.perform(get("/companies/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("token", "test-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/company/profile"));
    }

    @Test
    void shouldThrowExceptionForInvalidToken() throws Exception {
        company1.setEmailVerification(EmailVerification.PENDING);
        company1.setEmailConfirmationToken("test-token");

        mockMvc.perform(get("/companies/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("token", "invalid-token"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Invalid or already used token"));
    }

    @Test
    void shouldEditCompanyWithCorrectRequest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        user.setCompany(company1);
        userRepository.save(user);

        CompanyRequestDto edited = new CompanyRequestDto();
        edited.setName("Company C");
        edited.setEmail("company_a@abvz.com");
        edited.setCompanyTypeId(company1.getCompanyType().getId());

        String editedCompanyJson = objectMapper.writeValueAsString(edited);

        mockMvc.perform(post("/companies/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editedCompanyJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Company C"));
    }

    //TODO - Company image uploading tests
}