package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.ExperienceRequestDto;
import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Request.RateRequestDto;
import com.example.b2b_opportunities.Dto.Request.RequiredSkillsDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
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
import com.example.b2b_opportunities.Repository.WorkModeRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.Static.ProjectStatus;
import com.example.b2b_opportunities.UserDetailsImpl;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = true)
@Testcontainers
class PositionControllerTest {
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
    private WorkModeRepository workModeRepository;

    @Autowired
    private MockMvc mockMvc;

    private PositionRequestDto requestDto;
    private Authentication authentication;
    private User user2;
    private Project project;
    private PositionRole positionRole = new PositionRole();
    private CompanyType companyType;

    @BeforeEach
    void init() {
        // user role
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

        companyType = CompanyType.builder()
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

        project = new Project();
        project.setName("Test Project");
        project.setCompany(company);
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);

        company.setProjects(new ArrayList<>(List.of(project)));
        companyRepository.save(company);
        companyRepository.flush();

        positionRole = PositionRole.builder().name("Test position role").build();
        positionRole = positionRoleRepository.save(positionRole);

        // Create a sample PositionRequestDto with valid data
        requestDto = new PositionRequestDto();
        requestDto.setProjectId(project.getId());
        requestDto.setRole(positionRole.getId());
        requestDto.setSeniority(3L);
        requestDto.setWorkMode(List.of(1L, 2L));

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
        requestDto.setRequiredSkills(List.of(requiredSkillsDto));

        requestDto.setOptionalSkills(List.of(6L, 7L));
        requestDto.setMinYearsExperience(2);
        requestDto.setLocation(8L);
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
                .andExpect(jsonPath("$.statusId").value(1));

        List<Position> positions = positionRepository.findAll();
        assertThat(positions).hasSize(1);
        assertThat(positions.getFirst().getDescription()).isEqualTo("Position for software engineer");
    }

    @Test
    void shouldThrowErrorIfUserIsNotAssociatedWithTheCompany() throws Exception {
        // Authenticate with user that is not related to the company
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

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldGetAllTwoPositions() throws Exception {
        createPositionAndGetID();
        createPositionAndGetID();

        mockMvc.perform(get("/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].projectId").value(hasItem(project.getId().intValue())))
                .andExpect(jsonPath("$[*].role").value(hasItem(positionRole.getId().intValue())))
                .andExpect(jsonPath("$[*].seniority").value(hasItem(3)))
                .andExpect(jsonPath("$[0].workMode").value(containsInAnyOrder("HYBRID", "OFFICE")))
                .andExpect(jsonPath("$[0].rate").value(hasEntry("min", 50)))
                .andExpect(jsonPath("$[0].rate").value(hasEntry("max", 100)))
                .andExpect(jsonPath("$[0].rate").value(hasEntry("currency", "USD")))
                .andExpect(jsonPath("$[0].requiredSkills[0].skillId").value(4))
                .andExpect(jsonPath("$[0].requiredSkills[0].experience.months").value(6))
                .andExpect(jsonPath("$[0].requiredSkills[0].experience.years").value(2))
                .andExpect(jsonPath("$[0].minYearsExperience").value(2))
                .andExpect(jsonPath("$[0].hoursPerWeek").value(40))
                .andExpect(jsonPath("$[0].responsibilities").value(containsInAnyOrder("Develop software", "Review code")))
                .andExpect(jsonPath("$[0].hiringProcess").value("Interview -> Coding Test -> Offer"))
                .andExpect(jsonPath("$[0].description").value("Position for software engineer"));
    }

    @Test
    void shouldGetPosition() throws Exception {
        Long id = createPositionAndGetID();

        mockMvc.perform(get("/positions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(project.getId().intValue()))
                .andExpect(jsonPath("$.role").value(positionRole.getId().intValue()))
                .andExpect(jsonPath("$.statusId").value(1))
                .andExpect(jsonPath("$.seniority").value(3))
                .andExpect(jsonPath("$.workMode").value(containsInAnyOrder("HYBRID", "OFFICE")))
                .andExpect(jsonPath("$.rate").value(hasEntry("min", 50)))
                .andExpect(jsonPath("$.rate").value(hasEntry("max", 100)))
                .andExpect(jsonPath("$.rate").value(hasEntry("currency", "USD")))
                .andExpect(jsonPath("$.requiredSkills[0].skillId").value(4))
                .andExpect(jsonPath("$.requiredSkills[0].experience.months").value(6))
                .andExpect(jsonPath("$.requiredSkills[0].experience.years").value(2))
                .andExpect(jsonPath("$.minYearsExperience").value(2))
                .andExpect(jsonPath("$.hoursPerWeek").value(40))
                .andExpect(jsonPath("$.responsibilities").value(containsInAnyOrder("Develop software", "Review code")))
                .andExpect(jsonPath("$.hiringProcess").value("Interview -> Coding Test -> Offer"))
                .andExpect(jsonPath("$.description").value("Position for software engineer"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldGetEditedPosition() throws Exception {
        Long id = createPositionAndGetID();

        requestDto.setHoursPerWeek(20);

        mockMvc.perform(put("/positions/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/positions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(project.getId().intValue()))
                .andExpect(jsonPath("$.role").value(positionRole.getId().intValue()))
                .andExpect(jsonPath("$.statusId").value(1))
                .andExpect(jsonPath("$.seniority").value(3))
                .andExpect(jsonPath("$.workMode").value(containsInAnyOrder("HYBRID", "OFFICE")))
                .andExpect(jsonPath("$.rate").value(hasEntry("min", 50)))
                .andExpect(jsonPath("$.rate").value(hasEntry("max", 100)))
                .andExpect(jsonPath("$.rate").value(hasEntry("currency", "USD")))
                .andExpect(jsonPath("$.requiredSkills[0].skillId").value(4))
                .andExpect(jsonPath("$.requiredSkills[0].experience.months").value(6))
                .andExpect(jsonPath("$.requiredSkills[0].experience.years").value(2))
                .andExpect(jsonPath("$.minYearsExperience").value(2))
                .andExpect(jsonPath("$.hoursPerWeek").value(20))
                .andExpect(jsonPath("$.responsibilities").value(containsInAnyOrder("Develop software", "Review code")))
                .andExpect(jsonPath("$.hiringProcess").value("Interview -> Coding Test -> Offer"))
                .andExpect(jsonPath("$.description").value("Position for software engineer"));

        userRepository.deleteAll();
        companyTypeRepository.deleteAll();
        companyRepository.deleteAll();
        positionRepository.deleteAll();
        positionRoleRepository.deleteAll();
    }

    @Test
    void shouldDeleteExistingPosition() throws Exception {
        Long id = createPositionAndGetID();

        SecurityContextHolder.getContext().setAuthentication(authentication);
        mockMvc.perform(delete("/positions/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldThrowErrorIfPositionIsNotFound() throws Exception {
        long id = 9999L;

        String expectedMessage = "Position with ID: " + id + " not found";

        mockMvc.perform(delete("/positions/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/positions/" + id))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldThrowErrorIfUserIsTryingToAddPositionWithoutHavingCompany() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(user2);
        Authentication authenticationForUser2 = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationForUser2);

        mockMvc.perform(post("/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No company is associated with user " + user2.getUsername()));

    }

    @Test
    void shouldThrowErrorIfUserIsNotRelatedWithPosition() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(user2);
        Authentication authenticationForUser2 = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationForUser2);

        Company company2 = Company.builder()
                .name("Test Company2")
                .email("johndoe2@abv.bgg")
                .companyType(companyType)
                .website("https://www.x.com")
                .emailVerification(EmailVerification.ACCEPTED)
                .users(new ArrayList<>(List.of(user2)))
                .build();
        companyRepository.save(company2);

        user2.setCompany(company2);
        userRepository.save(user2);
        userRepository.flush();


        Project project2 = new Project();
        project2.setName("Test Project 2");
        project2.setCompany(company2);
        project2 = projectRepository.save(project2);

        company2.setProjects(new ArrayList<>(List.of(project2)));
        companyRepository.save(company2);
        companyRepository.flush();

        String expectedMessage = "Project ID: " + requestDto.getProjectId() + " is not associated with company ID: " + company2.getId() + " and user: " + user2.getUsername();

        mockMvc.perform(post("/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    private Long createPositionAndGetID() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MvcResult result = mockMvc.perform(post("/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(project.getId().intValue()))
                .andExpect(jsonPath("$.role").value(positionRole.getId().intValue()))
                .andExpect(jsonPath("$.statusId").value(1))
                .andExpect(jsonPath("$.seniority").value(3))
                .andExpect(jsonPath("$.workMode").value(containsInAnyOrder("HYBRID", "OFFICE")))
                .andExpect(jsonPath("$.rate").value(hasEntry("min", 50)))
                .andExpect(jsonPath("$.rate").value(hasEntry("max", 100)))
                .andExpect(jsonPath("$.rate").value(hasEntry("currency", "USD")))
                .andExpect(jsonPath("$.requiredSkills[0].skillId").value(4))
                .andExpect(jsonPath("$.requiredSkills[0].experience.months").value(6))
                .andExpect(jsonPath("$.requiredSkills[0].experience.years").value(2))
                .andExpect(jsonPath("$.minYearsExperience").value(2))
                .andExpect(jsonPath("$.hoursPerWeek").value(40))
                .andExpect(jsonPath("$.responsibilities").value(containsInAnyOrder("Develop software", "Review code")))
                .andExpect(jsonPath("$.hiringProcess").value("Interview -> Coding Test -> Offer"))
                .andExpect(jsonPath("$.description").value("Position for software engineer"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        PositionResponseDto positionResponseDto = objectMapper.readValue(responseBody, PositionResponseDto.class);
        return positionResponseDto.getId();
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