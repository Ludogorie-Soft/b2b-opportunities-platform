package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.CompanyType;
import com.example.b2b_opportunities.repository.CompanyRepository;
import com.example.b2b_opportunities.repository.CompanyTypeRepository;
import com.example.b2b_opportunities.enums.EmailVerification;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@Testcontainers
@ActiveProfiles("test")
public class AdminControllerTest {
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
    private CompanyRepository companyRepository;
    @Autowired
    private CompanyTypeRepository companyTypeRepository;
    @Autowired
    private MockMvc mockMvc;

    private CompanyType companyType = CompanyType.builder()
            .name("test")
            .build();
    private Company company = Company.builder()
            .companyType(companyType)
            .name("test")
            .email("test@test.test")
            .isApproved(false)
            .emailVerification(EmailVerification.ACCEPTED)
            .skills(new HashSet<>())
            .build();

    @Test
    public void shouldApproveCompanyThatIsNotApproved() throws Exception {
        companyTypeRepository.save(companyType);
        company = companyRepository.save(company);

        mockMvc.perform(post("/admin/approve/" + company.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("test")))
                .andExpect(jsonPath("$.approved", is(true)));
    }

    @Test
    public void shouldApproveCompany() throws Exception {
        companyTypeRepository.save(companyType);
        company.setApproved(true);
        company = companyRepository.save(company);

        mockMvc.perform(post("/admin/approve/" + company.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("test")))
                .andExpect(jsonPath("$.approved", is(true)));
    }

    @Test
    public void shouldGetTwoNotApprovedCompanies() throws Exception {
        companyRepository.deleteAll();
        companyTypeRepository.save(companyType);
        Company company1 = Company.builder()
                .companyType(companyType)
                .name("test")
                .email("test1@test.test")
                .isApproved(false)
                .emailVerification(EmailVerification.ACCEPTED)
                .skills(new HashSet<>())
                .build();

        Company company2 = Company.builder()
                .companyType(companyType)
                .name("test")
                .email("test2@test.test")
                .isApproved(true) // Approved - to make sure it's not being count
                .emailVerification(EmailVerification.ACCEPTED)
                .skills(new HashSet<>())
                .build();

        Company company3 = Company.builder()
                .companyType(companyType)
                .name("test")
                .email("test3@test.test")
                .isApproved(false)
                .emailVerification(EmailVerification.ACCEPTED)
                .skills(new HashSet<>())
                .build();

        companyRepository.saveAll(List.of(company1, company2, company3));

        mockMvc.perform(get("/admin/get-non-approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("test1@test.test"))
                .andExpect(jsonPath("$[1].email").value("test3@test.test"));
    }
}