package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.dto.requestDtos.PatternRequestDto;
import com.example.b2b_opportunities.entity.Pattern;
import com.example.b2b_opportunities.entity.Skill;
import com.example.b2b_opportunities.repository.PatternRepository;
import com.example.b2b_opportunities.repository.PositionRepository;
import com.example.b2b_opportunities.repository.SkillRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class PatternControllerTest {
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
    private PatternRepository patternRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private PositionRepository positionRepository;

    private Pattern pattern;

    @BeforeEach
    void setup() {
        positionRepository.deleteAll();
        patternRepository.deleteAll();
        skillRepository.deleteAll();

        Skill s1 = skillRepository.save(Skill.builder().name("first Skill").assignable(true).build());
        Skill s2 = skillRepository.save(Skill.builder().name("second Skill").assignable(true).build());

        List<Skill> skills = List.of(s1, s2);

        pattern = new Pattern();
        pattern.setName("Test Pattern");
        pattern.setSuggestedSkills(skills);
        pattern = patternRepository.save(pattern);
    }

    @Test
    void getAllPatternsSuccessfullyTest() throws Exception {
        performGetResult("/patterns")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].name").value("Test Pattern"));
    }

    @Test
    void getPatternByIdSuccessfullyTest() throws Exception {
        performGetResult("/patterns/" + pattern.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Pattern"));
    }

    @Test
    void getPatternByIdNotFoundTest() throws Exception {
        performGetResult("/patterns/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void createPatternSuccessfullyTest() throws Exception {
        PatternRequestDto newPattern = new PatternRequestDto();
        newPattern.setName("NewPa773rn");

        performPostResult(newPattern)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("NewPa773rn"));
    }

    @Test
    void updatePatternSuccessfullyTest() throws Exception {
        PatternRequestDto updatePattern = new PatternRequestDto();
        updatePattern.setId(pattern.getId());
        updatePattern.setName("UpdatedPa773rn");

        performPutResult(updatePattern)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedPa773rn"));
    }

    @Test
    void updatePatternIsNotFoundTest() throws Exception {
        PatternRequestDto updatePattern = new PatternRequestDto();
        updatePattern.setId(619L);
        updatePattern.setName("UpdatedPa773rn");

        performPutResult(updatePattern)
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePatternSuccessfullyTest() throws Exception {
        performDeleteResult("/patterns?id=" + pattern.getId())
                .andExpect(status().isNoContent());

        performGetResult("/patterns/" + pattern.getId())
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePatternNotFoundTest() throws Exception {
        performDeleteResult("/patterns?id=359")
                .andExpect(status().isNotFound());
    }

    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResultActions performGetResult(String url) throws Exception {
        return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPostResult(PatternRequestDto patternRequestDto) throws Exception {
        return mockMvc.perform(post("/patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(patternRequestDto)));
    }

    private ResultActions performPutResult(PatternRequestDto patternRequestDto) throws Exception {
        return mockMvc.perform(put("/patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(patternRequestDto)));
    }

    private ResultActions performDeleteResult(String url) throws Exception {
        return mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON));
    }
}