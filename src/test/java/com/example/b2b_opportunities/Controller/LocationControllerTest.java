package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.Location;
import com.example.b2b_opportunities.Repository.LocationRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class LocationControllerTest {
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
    private LocationRepository locationRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnLocationThatExist() throws Exception {
        String name = "Sofia";
        Location l = locationRepository.save(Location.builder().name(name).build());
        locationRepository.flush();
        mockMvc.perform(get("/locations/" + l.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(l.getId().intValue())))
                .andExpect(jsonPath("$.name", is(name)));
    }

    @Test
    void shouldThrowAnErrorWhenLocationDoesNotExist() throws Exception {
        long nonExistentId = 3333L;
        String expectedMessage = "Location with ID: " + nonExistentId + " not found";

        mockMvc.perform(get("/locations/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value("/locations/" + nonExistentId))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnTwoItems() throws Exception {
        locationRepository.save(Location.builder().name("Sofia").build());
        locationRepository.save(Location.builder().name("Varna").build());
        locationRepository.flush();
        mockMvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Sofia"))
                .andExpect(jsonPath("$[1].name").value("Varna"));
    }

    @Test
    void shouldCreateNewLocation() throws Exception {
        mockMvc.perform(post("/locations")
                        .param("name", "Plovdiv"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Plovdiv"));
    }

    @Test
    void shouldCreateNewLocationAndCapitalizeAndStripSpacesForAllWordsInLocation() throws Exception {
        mockMvc.perform(post("/locations")
                        .param("name", "  vEliKo    TarNovo  "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Veliko Tarnovo"));
    }

    @Test
    void shouldThrowAnErrorIfLocationAlreadyExists() throws Exception {
        String locationName = "Veliko Tarnovo";
        locationRepository.save(Location.builder().name(locationName).build());
        locationRepository.flush();

        String expectedMessage = "Location with name: '" + locationName + "' already exists";

        mockMvc.perform(post("/locations")
                        .param("name", "  vEliKo    TarNovo  "))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.path").value("/locations"))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldUpdateExistingLocationAndCapitalizeAndStripSpacesForAllWordsInLocation() throws Exception {
        Location l = locationRepository.save(Location.builder().name("Sofia").build());
        locationRepository.flush();
        mockMvc.perform(put("/locations/" + l.getId())
                        .param("newName", "  vEliKo    TarNovo  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Veliko Tarnovo"));
    }

    @Test
    void shouldReturnSameNameIfNewNameIsSameAsOldName() throws Exception {
        String locationName = "Veliko Tarnovo";
        Location l = locationRepository.save(Location.builder().name(locationName).build());
        locationRepository.flush();
        mockMvc.perform(put("/locations/" + l.getId())
                        .param("newName", "  vEliKo    TarNovo  "))
                .andExpect(jsonPath("$.name").value("Veliko Tarnovo"));
    }

    @Test
    void ShouldThrowAnErrorIfNewNameAlreadyExists() throws Exception {
        String locationName = "Veliko Tarnovo";
        locationRepository.save(Location.builder().name(locationName).build());
        Location l = locationRepository.save(Location.builder().name("Sofia").build());
        locationRepository.flush();

        String expectedMessage = "Location with name: '" + locationName + "' already exists";

        mockMvc.perform(put("/locations/" + l.getId())
                        .param("newName", "  vEliKo    TarNovo  "))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.path").value("/locations/" + l.getId().intValue()))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());

    }

    @Test
    void ShouldThrowAnErrorIfIdDoesNotExist() throws Exception {
        long id = 23322;
        String expectedMessage = "Location with ID: " + id + " not found";

        mockMvc.perform(put("/locations/" + id)
                        .param("newName", "Sofia"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/locations/" + id))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldDeleteItem() throws Exception {
        Location l = locationRepository.save(Location.builder().name("Sofia").build());
        locationRepository.flush();

        mockMvc.perform(delete("/locations/" + l.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldThrowErrorIfIdNotFoundWhenDeleting() throws Exception {
        long id = 12321214;
        String expectedMessage = "Location with ID: " + id + " not found";

        mockMvc.perform(delete("/locations/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/locations/" + id))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}