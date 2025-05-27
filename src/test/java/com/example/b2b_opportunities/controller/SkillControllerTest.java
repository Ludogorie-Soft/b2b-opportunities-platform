package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.entity.Skill;
import com.example.b2b_opportunities.repository.RequiredSkillRepository;
import com.example.b2b_opportunities.repository.SkillRepository;
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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@ActiveProfiles("test")
public class SkillControllerTest {
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
    private SkillRepository skillRepository;

    @Autowired
    private RequiredSkillRepository requiredSkillRepository;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldReturnASkillAsPartOfTheList() throws Exception {
        requiredSkillRepository.deleteAll();
        skillRepository.deleteAll();

        Skill skill = new Skill();
        skill.setName("testSkill");
        skill.setAssignable(false);
        skillRepository.save(skill);
        skillRepository.flush();

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(skill.getId().intValue())))
                .andExpect(jsonPath("$[*].name").value(hasItem("testSkill")));
    }

    @Test
    void shouldReturnANewSavedSkill() throws Exception {
        requiredSkillRepository.deleteAll();
        skillRepository.deleteAll();

        Skill skill = new Skill();
        skill.setName("testSkill");
        skill.setAssignable(false);
        skillRepository.save(skill);

        skillRepository.flush();

        mockMvc.perform(get("/skills/" + skill.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(skill.getId().intValue())))
                .andExpect(jsonPath("$.name", is("testSkill")));
    }

    @Test
    void shouldThrowExceptionWhenNonExistingSKillIdIsPassed() throws Exception {
        long nonExistingId = 9999999999L;
        String expectedMessage = "Skill with ID: " + nonExistingId + " not found";
        mockMvc.perform(get("/skills/" + nonExistingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/skills/" + "9999999999"))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
