package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.entity.Role;
import com.example.b2b_opportunities.entity.User;
import com.example.b2b_opportunities.exception.AuthenticationFailedException;
import com.example.b2b_opportunities.repository.UserRepository;
import com.example.b2b_opportunities.enums.RoleType;
import com.example.b2b_opportunities.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
@ActiveProfiles("test")
class UserControllerTest {
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
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUpUser() {
        Role role = new Role(1L, RoleType.ROLE_USER.name());
        user = User.builder()
                .username("oneUser")
                .firstName("testFirstName")
                .lastName("testLastname")
                .password("testPassword")
                .email("abvbg@abvto.bg")
                .isEnabled(true)
                .role(role).build();

        userRepository.save(user);
    }

    @Test
    void shouldThrowAuthenticationFailedWithNoAuthenticationProvidedTest() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldThrowAuthenticationFailedWhenAuthenticationIsNullTest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(AuthenticationFailedException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("Not authenticated", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void shouldThrowAuthenticationFailedWhenUserIsNotAuthenticatedTest() throws Exception {
        Authentication unauthenticated = Mockito.mock(Authentication.class);

        Mockito.when(unauthenticated.isAuthenticated()).thenReturn(false);

        SecurityContextHolder.getContext().setAuthentication(unauthenticated);

        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(AuthenticationFailedException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("Not authenticated", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void shouldRetrieveSuccessfullyUserDetailsWithUsernameAndPasswordTest() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("oneUser")))
                .andExpect(jsonPath("$.firstName", is("testFirstName")))
                .andExpect(jsonPath("$.lastName", is("testLastname")))
                .andExpect(jsonPath("$.email", is("abvbg@abvto.bg")))
                .andExpect(jsonPath("$.enabled", is(true)));
    }

    @Test
    void shouldAuthenticateAsUsernamePasswordAuthenticationTokenTest() {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
    }

    @Test
    void shouldRetrieveSuccessfullyUserDetailsWithOAuth2AuthenticationTest() throws Exception {
        Map<String, Object> attributesMap = new LinkedHashMap<>();
        attributesMap.put("email", "abvbg@abvto.bg");
        OAuth2User oAuth2User = Mockito.mock(OAuth2User.class);
        Mockito.when(oAuth2User.getAttributes()).thenReturn(attributesMap);

        OAuth2AuthenticationToken authenticationToken =
                new OAuth2AuthenticationToken(oAuth2User, new ArrayList<>(), "authClientRegId");
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("oneUser")))
                .andExpect(jsonPath("$.firstName", is("testFirstName")))
                .andExpect(jsonPath("$.lastName", is("testLastname")))
                .andExpect(jsonPath("$.email", is("abvbg@abvto.bg")))
                .andExpect(jsonPath("$.enabled", is(true)));
    }

    @Test
    void shouldThrowIllegalStateExceptionForUnsupportedAuthenticationTypeTest() throws Exception {
        Authentication unsupportedAuthType = Mockito.mock(Authentication.class);

        Mockito.when(unsupportedAuthType.isAuthenticated()).thenReturn(true);

        Mockito.when(unsupportedAuthType.getPrincipal()).thenReturn("Principal not supported!");

        SecurityContextHolder.getContext().setAuthentication(unsupportedAuthType);

        mockMvc.perform(get("/user"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(IllegalStateException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("Unsupported authentication type", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }
}
