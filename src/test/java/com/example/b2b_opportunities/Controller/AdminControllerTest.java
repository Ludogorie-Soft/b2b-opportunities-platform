package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    private User user = User.builder()
            .firstName("test")
            .lastName("test")
            .email("test@abv.bg")
            .username("test")
            .isApproved(false)
            .build();

    @Test
    public void shouldApproveUserThatIsNotApproved() throws Exception {
        user = userRepository.save(user);

        mockMvc.perform(post("/admin/approve/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("test")))
                .andExpect(jsonPath("$.approved", is(true)));
    }

    @Test
    public void shouldApproveUser() throws Exception {
        user.setApproved(true);
        user = userRepository.save(user);

        mockMvc.perform(post("/admin/approve/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("test")))
                .andExpect(jsonPath("$.approved", is(true)));
    }

    @Test
    public void shouldGetTwoNotApprovedUsers() throws Exception {
        User user1 = User.builder()
                .firstName("User1")
                .lastName("Test1")
                .email("user1@abv.bg")
                .username("user1")
                .isApproved(false)
                .build();

        User user2 = User.builder()
                .firstName("User2")
                .lastName("Test2")
                .email("user2@abv.bg")
                .username("user2")
                .isApproved(true)  // Approved - to make sure it's not being count
                .build();

        User user3 = User.builder()
                .firstName("User3")
                .lastName("Test3")
                .email("user3@abv.bg")
                .username("user3")
                .isApproved(false)
                .build();

        userRepository.saveAll(List.of(user1, user2, user3));

        mockMvc.perform(get("/admin/get-non-approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("user1@abv.bg"))
                .andExpect(jsonPath("$[1].email").value("user3@abv.bg"));
    }
}