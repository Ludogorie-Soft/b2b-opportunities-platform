package com.example.b2b_opportunities;

import com.example.b2b_opportunities.Dtos.Request.UserRequestDto;
import com.example.b2b_opportunities.Service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationService authenticationService;

    private UserRequestDto userRequestDto;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("testuser");
        userRequestDto.setFirstName("Test");
        userRequestDto.setLastName("Test");
        userRequestDto.setCompanyName("Test");
        userRequestDto.setEmail("test@test.com");
        userRequestDto.setPassword("password123");
        userRequestDto.setRepeatedPassword("password123");
    }

    @Test
    void registerUserWithValidInput() throws Exception {

        ResultActions resultActions = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "testuser",
                            "firstName": "Test",
                            "lastName": "Test",
                            "companyName": "Test",
                            "email": "test@test.com",
                            "password": "password123",
                            "repeatedPassword": "password123"
                        }
                        """));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("Test")))
                .andExpect(jsonPath("$.companyName", is("Test")))
                .andExpect(jsonPath("$.email", is("test@test.com")));
    }
}
