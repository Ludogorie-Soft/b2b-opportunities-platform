package com.example.b2b_opportunities;

import com.example.b2b_opportunities.Dtos.Request.UserRequestDto;
import com.example.b2b_opportunities.Dtos.Response.UserResponseDto;
import com.example.b2b_opportunities.Exceptions.ValidationException;
import com.example.b2b_opportunities.Service.AuthenticationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest extends BaseTest {

    @Autowired
    private AuthenticationService authenticationService;

    private final UserRequestDto user1 = new UserRequestDto(
            "user1",
            "George",
            "Washington",
            "CompanyOne",
            "george@company.com",
            "password123",
            "password123"
    );

    private final UserRequestDto user2 = new UserRequestDto(
            "user2",
            "John",
            "Doe",
            "CompanyTwo",
            "john@company.com",
            "password1234",
            "password1234"
    );

    @Test
    void shouldGetUsers() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        authenticationService.register(user1, bindingResult);
        authenticationService.register(user2, bindingResult);

        List<UserResponseDto> users = authenticationService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUsername());
        assertEquals("user2", users.get(1).getUsername());
    }

    @Test
    void testRegisterWithBindingErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            authenticationService.register(user1, bindingResult);
        });

        List<UserResponseDto> users = authenticationService.getAllUsers();
        assertEquals(0, users.size());
    }
}
