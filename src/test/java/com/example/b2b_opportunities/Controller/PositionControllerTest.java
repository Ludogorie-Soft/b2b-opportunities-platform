package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.ExperienceRequestDto;
import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Request.RateRequestDto;
import com.example.b2b_opportunities.Dto.Request.RequiredSkillsDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionRole;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Repository.PositionRepository;
import com.example.b2b_opportunities.Repository.PositionRoleRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Repository.RoleRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = true)
@Testcontainers
class PositionControllerTest {
    private final static String hostPath = Paths.get("Deploy/icons").toAbsolutePath().toString();
    private final static String containerPath = "/icons";

    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withExposedPorts(5432) // waits for the port to be available
            .withFileSystemBind(hostPath, containerPath, BindMode.READ_ONLY);


    @DynamicPropertySource
    public static void overridePropertiesFile(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CompanyTypeRepository companyTypeRepository;

    @Autowired
    private PositionRoleRepository positionRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MockMvc mockMvc;

    private PositionRequestDto requestDto;
    private Authentication authentication;
    private User user2;

    @BeforeEach
    void init() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@abv.bgg")
                .username("testUser")
                .isApproved(true)
                .isEnabled(true)
                .password("password")
                .role(roleRepository.findById(2L).orElseThrow()) // user role
                .build();

        user2 = User.builder()
                .firstName("John2")
                .lastName("Doe2")
                .email("johndoe2@abv.bgg")
                .username("testUser2")
                .isApproved(true)
                .isEnabled(true)
                .password("password2")
                .role(roleRepository.findById(2L).orElseThrow()) // user role
                .build();

        userRepository.save(user);
        userRepository.save(user2);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

//        SecurityContextHolder.getContext().setAuthentication(authentication);

        CompanyType companyType = CompanyType.builder()
                .name("test")
                .build();

        companyTypeRepository.save(companyType);

        Company company = Company.builder()
                .name("Test Company")
                .email("johndoe@abv.bgg")
                .companyType(companyType)
                .website("https://www.google.com")
                .emailVerification(EmailVerification.ACCEPTED)
                .users(new ArrayList<>(List.of(user)))
                .build();
        companyRepository.save(company);

        user.setCompany(company);
        userRepository.save(user);
        userRepository.flush();

        Project project = new Project();
        project.setName("Test Project");
        project.setCompany(company);
        project = projectRepository.save(project);

        company.setProjects(new ArrayList<>(List.of(project)));
        companyRepository.save(company);
        companyRepository.flush();

        PositionRole positionRole = PositionRole.builder().name("Test position role").build();
        positionRole = positionRoleRepository.save(positionRole);

        // Create a sample PositionRequestDto with valid data
        requestDto = new PositionRequestDto();
        requestDto.setProjectId(project.getId());
        requestDto.setRoleId(positionRole.getId());
        requestDto.setIsActive(true);
        requestDto.setSeniorityId(3L);
        requestDto.setWorkModeIds(List.of(1L, 2L));

        // Create a valid RateRequestDto
        RateRequestDto rateRequestDto = new RateRequestDto();
        rateRequestDto.setCurrency("USD");
        rateRequestDto.setMin(50);
        rateRequestDto.setMax(100);
        requestDto.setRate(rateRequestDto);

        // Create a valid RequiredSkillsDto
        RequiredSkillsDto requiredSkillsDto = new RequiredSkillsDto();
        requiredSkillsDto.setSkillId(4L);
        // Create a valid ExperienceRequestDto for Skills(not required)
        ExperienceRequestDto experienceRequestDto = new ExperienceRequestDto();
        experienceRequestDto.setMonths(6);
        experienceRequestDto.setYears(2);
        requiredSkillsDto.setExperienceRequestDto(experienceRequestDto);
        requestDto.setRequiredSkillsList(List.of(requiredSkillsDto));

        requestDto.setOptionalSkillsList(List.of(6L, 7L));
        requestDto.setMinYearsExperience(2);
        requestDto.setLocationId(8L);
        requestDto.setHoursPerWeek(40);
        requestDto.setResponsibilities(List.of("Develop software", "Review code"));
        requestDto.setHiringProcess("Interview -> Coding Test -> Offer");
        requestDto.setDescription("Position for software engineer");
    }

    @Test
    void shouldCreatePositionSuccessfully() throws Exception {
        // Authorize with correct user
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Position for software engineer"))
                .andExpect(jsonPath("$.isActive").value(true));

        List<Position> positions = positionRepository.findAll();
        assertThat(positions).hasSize(1);
        assertThat(positions.getFirst().getDescription()).isEqualTo("Position for software engineer");
    }

    @Test
    void shouldThrowErrorIfUserIsNotAssociatedWithTheCompany() throws Exception {
        // Authorize with user that is not related to the company
        UserDetailsImpl userDetails = new UserDetailsImpl(user2);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String expectedMessage = "No company is associated with user " + user2.getUsername();

        mockMvc.perform(post("/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/positions"))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldThrowErrorIfUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // Helper method to convert an object to JSON string
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}