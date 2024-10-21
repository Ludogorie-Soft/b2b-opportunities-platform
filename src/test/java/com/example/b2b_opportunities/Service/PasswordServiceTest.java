package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.ResetPasswordDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.PasswordsNotMatchingException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class PasswordServiceTest {
    @Mock
    private MailService mailService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;
    @Mock
    private HttpServletRequest request;
    @InjectMocks
    private PasswordService passwordService;
    @Mock
    private AuthenticationService authenticationService;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .firstName("test")
                .lastName("test")
                .email("test@abv.bg")
                .username("test")
                .isApproved(true)
                .isEnabled(true)
                .provider(null)
                .build();
    }

    @Test
    void testRequestPasswordRecovery_Success() {
        when(userRepository.findByEmail("test@abv.bg")).thenReturn(Optional.of(user));
        doNothing().when(mailService).sendPasswordRecoveryMail(any(User.class), any(HttpServletRequest.class));

        String result = passwordService.requestPasswordRecovery("test@abv.bg", request);

        assertEquals("Password recovery e-mail was sent successfully", result);
        assertNotNull(result);
    }

    @Test
    void testRequestPasswordRecovery_NotRegisteredUser() {
        when(userRepository.findByEmail("nonexistent@abv.bg")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                passwordService.requestPasswordRecovery("nonexistent@abv.bg", request));

        assertEquals("User not registered", exception.getMessage());
    }

    @Test
    void testRequestPasswordRecovery_OauthUser() {
        when(userRepository.findByEmail("test@abv.bg")).thenReturn(Optional.of(user));
        user.setProvider("testProvider");

        PermissionDeniedException exception = assertThrows(PermissionDeniedException.class, () ->
                passwordService.requestPasswordRecovery("test@abv.bg", request));

        assertEquals("User registered using oAuth - " + user.getProvider(), exception.getMessage());
    }

    @Test
    void testRequestPasswordRecovery_disabledUser(){
        when(userRepository.findByEmail("test@abv.bg")).thenReturn(Optional.of(user));
        user.setProvider(null);
        user.setEnabled(false);

        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class, () ->
                passwordService.requestPasswordRecovery("test@abv.bg", request));

        assertEquals("Account not activated", exception.getMessage());
    }

    @Test
    void testSetNewPassword_Success() {
        user.setPassword("oldPassword");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setCreatedAt(LocalDateTime.now());
        confirmationToken.setUser(user);
        confirmationToken.setToken("validToken");

        when(authenticationService.validateAndReturnToken("validToken")).thenReturn(confirmationToken);

        when(authenticationService.arePasswordsMatching("newPassword", "newPassword")).thenReturn(true);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("validToken", "newPassword", "newPassword");

        String result = passwordService.setNewPassword(resetPasswordDto);

        assertEquals("Password changed successfully", result);
        assertNotEquals("oldPassword", user.getPassword());
    }

    @Test
    void testSetNewPassword_WithExpiredToken() {
        user.setPassword("oldPassword");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setCreatedAt(LocalDateTime.now().minusDays(10)); //set expired token
        confirmationToken.setUser(user);
        confirmationToken.setToken("expiredToken");

        when(confirmationTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(confirmationToken));

        when(authenticationService.validateAndReturnToken("expiredToken")).thenThrow(new InvalidRequestException("Expired token"));

        when(authenticationService.arePasswordsMatching("newPassword", "newPassword")).thenReturn(true);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("expiredToken", "newPassword", "newPassword");

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> passwordService.setNewPassword(resetPasswordDto));

        assertEquals("Expired token", exception.getMessage());
        assertEquals("oldPassword", user.getPassword());
    }

    @Test
    void testSetNewPassword_WithInvalidToken() {
        user.setPassword("oldPassword");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setCreatedAt(LocalDateTime.now()); //set expired token
        confirmationToken.setUser(user);
        confirmationToken.setToken("validToken");

        when(confirmationTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        when(authenticationService.validateAndReturnToken("invalidToken")).thenThrow(new InvalidRequestException("Invalid token"));

        when(authenticationService.arePasswordsMatching("newPassword", "newPassword")).thenReturn(true);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("invalidToken", "newPassword", "newPassword");

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> passwordService.setNewPassword(resetPasswordDto));

        assertEquals("Invalid token", exception.getMessage());
        assertEquals("oldPassword", user.getPassword());
    }

    @Test
    void testSetNewPassword_WithNotMatchingPasswords() {
        user.setPassword("oldPassword");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setCreatedAt(LocalDateTime.now());
        confirmationToken.setUser(user);
        confirmationToken.setToken("validToken");

        when(authenticationService.validateAndReturnToken("validToken")).thenReturn(confirmationToken);

        when(authenticationService.arePasswordsMatching("newPassword", "anotherPassword")).thenReturn(false);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("validToken", "newPassword", "anotherPassword");

        PasswordsNotMatchingException exception = assertThrows(PasswordsNotMatchingException.class, () -> passwordService.setNewPassword(resetPasswordDto));

        assertEquals("Passwords do not match", exception.getMessage());
        assertEquals("oldPassword", user.getPassword());
    }

}