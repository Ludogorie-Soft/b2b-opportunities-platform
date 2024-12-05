package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Service.AuthenticationService;
import com.example.b2b_opportunities.Service.MailService;
import com.example.b2b_opportunities.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional  // Roll back changes made by tests, ensuring each test starts with a clean state.
@Testcontainers
@ActiveProfiles("test")
public class AuthControllerTest {
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

    @MockitoSpyBean
    private MailService mailService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    private UserService userService;

    private final UserRequestDto userRequestDto = new UserRequestDto(
            "Test-User",
            "Test",
            "User",
            "TestUSER@example.Com",
            "password123",
            "password123"
    );

    @BeforeEach
    void setUp() {
        doNothing().when(mailService).sendConfirmationMail(any(), any());
    }

    @Test
    void shouldRegisterUserAndSaveToDatabase() throws Exception {
        // Convert the DTO to JSON
        String userRequestJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("test-user")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.email", is("testuser@example.com")));

        assertTrue(authenticationService.isUsernameInDB("test-user"));
    }

    @Test
    void shouldNotRegisterSameEmailTwice() throws Exception {
        // Convert the DTO to JSON
        String userRequestJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson))
                .andExpect(status().isCreated());

        UserRequestDto userRequestDto2 = userRequestDto;
        userRequestDto2.setUsername("new-user-name");
        String userRequestJson2 = objectMapper.writeValueAsString(userRequestDto2);

        // Test with same email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Email already in use. Please use a different email")));

        assertTrue(authenticationService.isUsernameInDB("test-user"));

        List<UserResponseDto> users = authenticationService.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    void shouldNotRegisterSameUsernameTwice() throws Exception {
        // Convert the DTO to JSON
        String userRequestJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson))
                .andExpect(status().isCreated());

        UserRequestDto userRequestDto2 = userRequestDto;
        userRequestDto2.setEmail("new-free-email@abv.bg");
        String userRequestJson2 = objectMapper.writeValueAsString(userRequestDto2);

        // Test with same username
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Username already in use. Please use a different username")));

        assertTrue(authenticationService.isUsernameInDB("test-user"));

        List<UserResponseDto> users = authenticationService.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    void shouldNotRegisterUserWithWrongRepeatPassword() throws Exception {
        // Convert the DTO to JSON
        String userRequestJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson))
                .andExpect(status().isCreated());

        UserRequestDto userRequestDto2 = userRequestDto;
        userRequestDto2.setEmail("new-free-email@abv.bg");
        userRequestDto2.setUsername("new-free-username");
        userRequestDto2.setRepeatedPassword("wrong-password");
        String userRequestJson2 = objectMapper.writeValueAsString(userRequestDto2);

        // Test with wrong second password
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Passwords don't match")));

        assertTrue(authenticationService.isUsernameInDB("test-user"));
        assertFalse(authenticationService.isUsernameInDB("new-free-username"));

        List<UserResponseDto> users = authenticationService.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
        // Disable transaction for this method
    void shouldLoginAndReturnToken() throws Exception {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        String userRequestJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequestJson));

        transactionManager.commit(transaction);

        Thread.sleep(1000);

        User user = userService.getUserByEmailOrThrow(userRequestDto.getEmail().toLowerCase());

        user.setEnabled(true);
        userRepository.save(user);

        userRepository.flush();

        LoginDto loginDto = new LoginDto(userRequestDto.getEmail(), userRequestDto.getPassword());

        String loginDtoJson = objectMapper.writeValueAsString(loginDto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDtoJson))
                .andExpect(status().isOk());

        userRepository.delete(user);
    }

    @Test
    void testResendRegistrationMail() throws Exception {
        String userRequestJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequestJson));

        User user = userService.getUserByEmailOrThrow(userRequestDto.getEmail().toLowerCase());
        assertFalse(user.isEnabled());

        mockMvc.perform(get("/api/auth/register/resend-confirmation")
                        .param("email", user.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content().string("A new token was sent to your e-mail!"));
    }

    @Test
    void testConfirmEmail() throws Exception {
        String userRequestJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequestJson));

        User user = userService.getUserByEmailOrThrow(userRequestDto.getEmail().toLowerCase());
        assertFalse(user.isEnabled());
        String token = mailService.createAndSaveUserToken(user);

        mockMvc.perform(get("/api/auth/register/confirm")
                        .param("token", token))
                .andExpect(status().isFound()) // Change this line to expect 302
                .andExpect(header().string("Location", "http://localhost:5173/signup?confirmEmail=true"));

        User confirmedUser = userService.getUserByEmailOrThrow(userRequestDto.getEmail().toLowerCase());
        assertTrue(confirmedUser.isEnabled());
    }

    @Test
    void testOAuthLoginCreatesNewUser() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@test.com");
        attributes.put("given_name", "Test");
        attributes.put("family_name", "User");

        OAuth2User mockOAuth2User = new CustomOAuth2User(attributes);
        OAuth2AuthenticationToken mockOAuth2Token = new OAuth2AuthenticationToken(
                mockOAuth2User, null, "google");

        mockMvc.perform(get("/api/auth/oauth2/success").principal(mockOAuth2Token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/company/profile"));

        User newUser = userService.getUserByEmailOrThrow("test@test.com");

        assertEquals("Test", newUser.getFirstName());
        assertEquals("User", newUser.getLastName());
        assertTrue(newUser.isEnabled());
        assertEquals("google", newUser.getProvider());
    }

    private static class CustomOAuth2User implements OAuth2User {
        private final Map<String, Object> attributes;

        CustomOAuth2User(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of();
        }

        @Override
        public String getName() {
            return (String) attributes.get("email");
        }
    }
}