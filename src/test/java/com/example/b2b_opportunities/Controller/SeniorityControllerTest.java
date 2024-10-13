package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.Seniority;
import com.example.b2b_opportunities.Repository.SeniorityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import jakarta.transaction.Transactional;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Testcontainers
public class SeniorityControllerTest {

    private static final String HOST_PATH = Paths.get("Deploy/icons").toAbsolutePath().toString();
    private static final String CONTAINER_PATH = "/icons";

    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withExposedPorts(5432)
            .withFileSystemBind(HOST_PATH, CONTAINER_PATH, BindMode.READ_ONLY);


    @DynamicPropertySource
    public static void overridePropertiesFile(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SeniorityRepository seniorityRepository;

    @BeforeEach
    void setupSeniority() {
        seniorityRepository.deleteAll();

        Seniority primary = new Seniority();
        primary.setId(1L);
        primary.setLabel("Primary");
        primary.setLevel((short) 1);

        seniorityRepository.save(primary);

        Seniority secondary = new Seniority();
        secondary.setId(2L);
        secondary.setLabel("Secondary");
        secondary.setLevel((short) 2);

        seniorityRepository.save(secondary);

        Seniority intermediate = new Seniority();
        intermediate.setId(3L);
        intermediate.setLabel("Intermediate");
        intermediate.setLevel((short) 3);

        seniorityRepository.save(intermediate);
    }

    private ResultActions performGetResult(String url) throws Exception {
        return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAllSenioritySuccessfullyTest() throws Exception {
        performGetResult("/seniorities")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].label").value("Primary"))
                .andExpect(jsonPath("$[0].level").value(1))
                .andExpect(jsonPath("$[1].label").value("Secondary"))
                .andExpect(jsonPath("$[1].level").value(2))
                .andExpect(jsonPath("$[2].label").value("Intermediate"))
                .andExpect(jsonPath("$[2].level").value(3));
    }

    @Test
    void getSeniorityByIdWithDifferentIdsSuccessfullyTest() throws Exception {
        performGetResult("/seniorities/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Primary"))
                .andExpect(jsonPath("$.level").value(1));
        performGetResult("/seniorities/2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Secondary"))
                .andExpect(jsonPath("$.level").value(2));
        performGetResult("/seniorities/3")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Intermediate"))
                .andExpect(jsonPath("$.level").value(3));
    }

    @Test
    void getSeniorityByIdShouldBeNotExistingSeniorityTest() throws Exception {
        performGetResult("/seniorities/619")
                .andExpect(status().isNotFound());
    }

    @Test
    void getSeniorityByNegativeIdShouldBeNotExistingSeniorityTest() throws Exception {
        performGetResult("/seniorities/-619")
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllSeniorityWhenEmptyShouldReturnEmptyListTest() throws Exception {
        seniorityRepository.deleteAll();

        performGetResult("/seniorities")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getSeniorityByLargePayloadShouldReturnOkTest() throws Exception {
        seniorityRepository.deleteAll();

        for (int i = 1; i <= 5000; i++) {
            Seniority seniority = new Seniority();
            seniority.setId((long) i);
            seniority.setLabel("Seniority " + i);
            seniority.setLevel((short) i);
            seniorityRepository.save(seniority);
        }

        performGetResult("/seniorities")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5000));
    }

    @Test
    void getSeniorityByStringShouldThrowInternalServerError() throws Exception {
        performGetResult("/seniorities/alabalaportokala")
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}
