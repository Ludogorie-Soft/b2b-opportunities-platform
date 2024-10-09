package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Repository.DomainRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
class DomainControllerTest {
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
    DomainRepository domainRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldSaveDomainAndReturnItAsPartOfTheList() throws Exception {
        String domainName = "testDomain";
        Domain domain = domainRepository.save(Domain.builder().name(domainName).build());
        domainRepository.flush();
        mockMvc.perform(get("/domains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(domain.getId().intValue())))
                .andExpect(jsonPath("$[*].name").value(hasItem(domainName)));
    }

    @Test
    void shouldReturnDomain() throws Exception {
        String domainName = "testDomain";
        Domain domain = domainRepository.save(Domain.builder().name(domainName).build());
        domainRepository.flush();
        mockMvc.perform(get("/domains/" + domain.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(domain.getId().intValue())))
                .andExpect(jsonPath("$.name", is(domainName)));
    }

    @Test
    void shouldThrowAnExceptionWhenDomainExists() throws Exception {
        String domainName = "Existing";

        domainRepository.save(Domain.builder().name(domainName).build());
        domainRepository.flush();

        String expectedMessage = "Domain with name: '" + domainName + "' already exists";

        mockMvc.perform(post("/domains")
                        .param("name", "existing"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFormatAndSaveDomainNameWhenNeeded() throws Exception {
        String domainName = "test";
        mockMvc.perform(post("/domains")
                        .param("name", domainName))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test")));
    }

    @Test
    void shouldThrowAnErrorWhenEnteringExistingDomainName() throws Exception {
        String existingDomainName = "First";
        String anotherDomainName = "Second";
        domainRepository.save(Domain.builder().name(existingDomainName).build());
        Domain domainToBeEdited = domainRepository.save(Domain.builder().name(anotherDomainName).build());
        domainRepository.flush();

        String expectedMessage = "Domain with name: '" + existingDomainName + "' already exists";

        mockMvc.perform(put("/domains/" + domainToBeEdited.getId())
                        .param("newName", existingDomainName))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldThrowAnErrorWhenDomainIdDoesNotExist() throws Exception {
        long nonExistingId = 999999999L;

        String expectedMessage = "Domain with ID: " + nonExistingId + " not found";

        mockMvc.perform(get("/domains/" + nonExistingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/domains/" + nonExistingId))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingId() throws Exception {
        long nonExistingId = 999999999L;

        String expectedMessage = "Domain with ID: " + nonExistingId + " not found";

        mockMvc.perform(delete("/domains/" + nonExistingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/domains/" + nonExistingId))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldDeleteWhenIdIsFound() throws Exception {
        Domain domainToBeDeleted = domainRepository.save(Domain.builder().name("deleted").build());
        domainRepository.flush();
        mockMvc.perform(delete("/domains/" + domainToBeDeleted.getId()))
                .andExpect(status().isNoContent());
    }
}
