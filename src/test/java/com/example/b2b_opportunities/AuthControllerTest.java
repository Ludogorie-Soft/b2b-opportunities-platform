package com.example.b2b_opportunities;

import com.example.b2b_opportunities.Dtos.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dtos.Request.UserRequestDto;
import com.example.b2b_opportunities.Dtos.Response.UserResponseDto;
import com.example.b2b_opportunities.Service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional  // Roll back changes made by tests, ensuring each test starts with a clean state.
public class AuthControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationService authenticationService;

    private final UserRequestDto userRequestDto = new UserRequestDto(
            "Test-User",
            "Test",
            "User",
            "TestCompany",
            "TestUSER@example.Com",
            "password123",
            "password123"
    );

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
    void shouldLoginAndReturnToken() throws Exception {

        String userRequestJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequestJson));

        LoginDto loginDto = new LoginDto(userRequestDto.getEmail(), userRequestDto.getPassword());

        String loginDtoJson = objectMapper.writeValueAsString(loginDto);


        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDtoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn", is(3600000)));
    }
}