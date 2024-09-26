package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.BaseTest;
import com.example.b2b_opportunities.Dto.Request.ResetPasswordDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.DisabledUserException;
import com.example.b2b_opportunities.Exception.InvalidTokenException;
import com.example.b2b_opportunities.Exception.OAuthUserPasswordResetException;
import com.example.b2b_opportunities.Exception.PasswordsNotMatchingException;
import com.example.b2b_opportunities.Exception.UserNotFoundException;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class PasswordServiceTest extends BaseTest {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    private final HttpServletRequest request;

    private final UserRepository userRepository;

    private final PasswordService passwordService;

    private User user;
    private ConfirmationToken token;

    @Autowired
    public PasswordServiceTest(HttpServletRequest request, UserRepository userRepository, PasswordService passwordService, ConfirmationTokenRepository confirmationTokenRepository) {
        this.request = request;
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .firstName("test")
                .lastName("test")
                .email("test@abv.bg")
                .username("test")
                .build();
    }

    @Test
    void testRequestPasswordRecovery_Success() {
        user.setApproved(true);
        user.setEnabled(true);
        user.setProvider(null);

        user = userRepository.save(user);

        String result = passwordService.requestPasswordRecovery("test@abv.bg", request);
        assertEquals(result, "Password recovery e-mail was sent successfully");
        assertNotNull(result);
    }

    @Test
    void testRequestPasswordRecovery_NotRegisteredUser() {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                passwordService.requestPasswordRecovery("nonexistent@abv.bg", request));
        assertEquals("User not registered", exception.getMessage());
    }

    @Test
    void testRequestPasswordRecovery_OauthUser() {
        user.setApproved(true);
        user.setEnabled(true);
        user.setProvider("test");

        user = userRepository.save(user);

        OAuthUserPasswordResetException exception = assertThrows(OAuthUserPasswordResetException.class, () ->
                passwordService.requestPasswordRecovery("test@abv.bg", request));

        assertEquals("User registered using oAuth - " + user.getProvider(), exception.getMessage());
    }

    @Test
    void testRequestPasswordRecovery_disabledUser() {
        user.setApproved(true);
        user.setProvider(null);

        user = userRepository.save(user);

        DisabledUserException exception = assertThrows(DisabledUserException.class, () ->
                passwordService.requestPasswordRecovery("test@abv.bg", request));

        assertEquals("Account not activated", exception.getMessage());
    }

    @Test
    void testSetNewPassword_Success() {
        user = userRepository.save(user);

        token = new ConfirmationToken();
        token.setCreatedAt(LocalDateTime.now());
        token.setUser(user);
        token.setToken("validToken");

        confirmationTokenRepository.save(token);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("validToken", "newPassword", "newPassword");

        String result = passwordService.setNewPassword(resetPasswordDto);

        assertEquals("Password changed successfully", result);
        assertNotEquals("oldPassword", user.getPassword());
    }

    @Test
    void testSetNewPassword_WithExpiredToken() {
        user = userRepository.save(user);

        token = new ConfirmationToken();
        token.setCreatedAt(LocalDateTime.now().minusDays(4));
        token.setUser(user);
        token.setToken("expiredToken");

        confirmationTokenRepository.save(token);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("expiredToken", "newPassword", "newPassword");

        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> passwordService.setNewPassword(resetPasswordDto));

        assertEquals("Expired token", exception.getMessage());
    }

    @Test
    void testSetNewPassword_WithInvalidToken() {
        user = userRepository.save(user);

        token = new ConfirmationToken();
        token.setCreatedAt(LocalDateTime.now());
        token.setUser(user);
        token.setToken("token");

        confirmationTokenRepository.save(token);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("invalid-token", "newPassword", "newPassword");

        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> passwordService.setNewPassword(resetPasswordDto));

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void testSetNewPassword_WithNotMatchingPasswords() {
        user = userRepository.save(user);

        token = new ConfirmationToken();
        token.setCreatedAt(LocalDateTime.now());
        token.setUser(user);
        token.setToken("token");

        confirmationTokenRepository.save(token);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("token", "newPassword", "anotherPassword");

        PasswordsNotMatchingException exception = assertThrows(PasswordsNotMatchingException.class, () -> passwordService.setNewPassword(resetPasswordDto));

        assertEquals("Passwords do not match", exception.getMessage());
    }


}
