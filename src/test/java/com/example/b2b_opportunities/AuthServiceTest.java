package com.example.b2b_opportunities;

import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Exception.ValidationException;
import com.example.b2b_opportunities.Service.AuthenticationService;
import com.example.b2b_opportunities.Service.MailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest extends BaseTest {

    @MockBean
    private MailService mailService;
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

        HttpServletRequest request = mock(HttpServletRequest.class);

        doNothing().when(mailService).sendConfirmationMail(any(), any());

        authenticationService.register(user1, bindingResult, request);
        authenticationService.register(user2, bindingResult, request);

        List<UserResponseDto> users = authenticationService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUsername());
        assertEquals("user2", users.get(1).getUsername());
    }

    @Test
    void testRegisterWithBindingErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThrows(ValidationException.class, () -> {
            authenticationService.register(user1, bindingResult, request);
        });

        List<UserResponseDto> users = authenticationService.getAllUsers();
        assertEquals(0, users.size());
    }
}
