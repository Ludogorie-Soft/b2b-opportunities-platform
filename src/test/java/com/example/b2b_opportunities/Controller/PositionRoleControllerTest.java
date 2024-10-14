package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.PositionRole;
import com.example.b2b_opportunities.Repository.PositionRoleRepository;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
class PositionRoleControllerTest {
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
    private PositionRoleRepository positionRoleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnPositionRoleThatExist() throws Exception {
        String name = "role";
        PositionRole p = positionRoleRepository.save(PositionRole.builder().name(name).build());
        positionRoleRepository.flush();
        mockMvc.perform(get("/roles/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(p.getId().intValue())))
                .andExpect(jsonPath("$.name", is(name)));
    }

    @Test
    void shouldThrowAnErrorWhenPositionRoleDoesNotExist() throws Exception {
        long nonExistentId = 3333L;
        String expectedMessage = "Role with ID: " + nonExistentId + " not found";

        mockMvc.perform(get("/roles/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value("/roles/" + nonExistentId))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnTwoItems() throws Exception {
        positionRoleRepository.save(PositionRole.builder().name("position1").build());
        positionRoleRepository.save(PositionRole.builder().name("position2").build());
        positionRoleRepository.flush();
        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("position1"))
                .andExpect(jsonPath("$[1].name").value("position2"));
    }

    @Test
    void shouldCreateNewPositionRoleAndCapitalizeAndStripSpacesForAllWords() throws Exception {
        mockMvc.perform(post("/roles")
                        .param("name", "  my tEst Role  "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Test Role"));
    }

    @Test
    void shouldThrowAnErrorIfPositionRoleAlreadyExists() throws Exception {
        String roleName = "My Role";
        positionRoleRepository.save(PositionRole.builder().name(roleName).build());
        positionRoleRepository.flush();

        String expectedMessage = "Role with name: '" + roleName + "' already exists";

        mockMvc.perform(post("/roles")
                        .param("name", "  mY   rOlE  "))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.path").value("/roles"))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldUpdateExistingPositionRoleAndCapitalizeAndStripSpacesForAllWords() throws Exception {
        PositionRole p = positionRoleRepository.save(PositionRole.builder().name("Role").build());
        positionRoleRepository.flush();
        mockMvc.perform(put("/roles/" + p.getId())
                        .param("newName", "  nEw Role pOsiTion  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Role Position"));
    }

    @Test
    void shouldReturnSameNameIfNewNameIsSameAsOldName() throws Exception {
        String positionName = "My Role";
        PositionRole p = positionRoleRepository.save(PositionRole.builder().name(positionName).build());
        positionRoleRepository.flush();
        mockMvc.perform(put("/roles/" + p.getId())
                        .param("newName", "  my Role  "))
                .andExpect(jsonPath("$.name").value("My Role"));
    }

    @Test
    void ShouldThrowAnErrorIfNewNameAlreadyExists() throws Exception {
        String position = "Role1";
        positionRoleRepository.save(PositionRole.builder().name(position).build());
        PositionRole p = positionRoleRepository.save(PositionRole.builder().name("Role2").build());

        positionRoleRepository.flush();

        String expectedMessage = "Role with name: '" + position + "' already exists";

        mockMvc.perform(put("/roles/" + p.getId())
                        .param("newName", "role1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.path").value("/roles/" + p.getId().intValue()))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}