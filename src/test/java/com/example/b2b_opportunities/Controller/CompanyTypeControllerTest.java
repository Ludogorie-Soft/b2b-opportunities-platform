package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.PositionRole;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
class CompanyTypeControllerTest {
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
    private CompanyTypeRepository companyTypeRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCompanyTypeThatExists() throws Exception {
        String name = "company type";
        CompanyType ct = companyTypeRepository.save(CompanyType.builder().name(name).build());
        companyTypeRepository.flush();
        mockMvc.perform(get("/company-types/" + ct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ct.getId().intValue())))
                .andExpect(jsonPath("$.name", is(name)));
    }

    @Test
    void shouldThrowExceptionWhenCompanyTypeDoesNotExist() throws Exception {
        long nonExistentId = 999999999L;
        String expectedMessage = "Company type with ID: " + nonExistentId + " not found";

        mockMvc.perform(get("/company-types/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value("/company-types/" + nonExistentId))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldSaveCompanyTypeAndReturnItAsPartOfTheList() throws Exception {
        String name = "company type";
        CompanyType ct = companyTypeRepository.save(CompanyType.builder().name(name).build());
        companyTypeRepository.flush();
        mockMvc.perform(get("/company-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(ct.getId().intValue())))
                .andExpect(jsonPath("$[*].name").value(hasItem(name)));
    }

    @Test
    void shouldCreateNewCompanyTypeAndStripAndCapitalizeTheName() throws Exception{
        mockMvc.perform(post("/roles")
                        .param("name", " cOmPanY    tyPe  "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Company Type"));
    }

    @Test
    void shouldThrowAnExceptionWhenDomainExists() throws Exception {
        String name = "Existing";

        companyTypeRepository.save(CompanyType.builder().name(name).build());
        companyTypeRepository.flush();

        String expectedMessage = "Company type with name: '" + name + "' already exists";

        mockMvc.perform(post("/company-types")
                        .param("name", "existing"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldUpdateExistingCompanyTypeAndStripAndCapitalizeTheName() throws Exception {
        CompanyType ct = companyTypeRepository.save(CompanyType.builder().name("Test Company Type").build());
        companyTypeRepository.flush();
        mockMvc.perform(put("/company-types/" + ct.getId())
                        .param("newName", "  NeW CompANy tYPe  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Company Type"));
    }

    @Test
    void shouldReturnSameNameIfNewNameIsSameAsOldName() throws Exception {
        String name = "Company Type";
        CompanyType ct = companyTypeRepository.save(CompanyType.builder().name(name).build());
        companyTypeRepository.flush();
        mockMvc.perform(put("/company-types/" + ct.getId())
                        .param("newName", "  coMPANY       tYPe   "))
                .andExpect(jsonPath("$.name").value("Company Type"));
    }

    @Test
    void ShouldThrowAnErrorIfNewNameAlreadyExists() throws Exception {
        String name = "Company Type";
        companyTypeRepository.save(CompanyType.builder().name(name).build());
        CompanyType ct = companyTypeRepository.save(CompanyType.builder().name("Another Company Type").build());

        companyTypeRepository.flush();

        String expectedMessage = "Company type with name: '" + name + "' already exists";

        mockMvc.perform(put("/company-types/" + ct.getId())
                        .param("newName", "Company Type"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.path").value("/company-types/" + ct.getId().intValue()))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
