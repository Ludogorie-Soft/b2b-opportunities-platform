package com.example.b2b_opportunities;

import com.example.b2b_opportunities.Dto.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dto.LoginDtos.LoginResponse;
import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.*;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Service.AuthenticationService;
import com.example.b2b_opportunities.Service.JwtService;
import com.example.b2b_opportunities.Service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private MailService mailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginWithValidInput() {

        LoginDto loginDto = new LoginDto("test@test.com","password");
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password");

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("test-jwt-token");
        when(jwtService.getExpirationTime())
                .thenReturn(3600L);

        ResponseEntity<LoginResponse> response = authenticationService.login(loginDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-jwt-token", response.getBody().getToken());
        assertEquals(3600L, response.getBody().getExpiresIn());
    }

    @Test
    public void testLoginWithWrongPassword() {

        LoginDto loginDto = new LoginDto("test@test.com","wrong-password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new AuthenticationException("") {
                });

        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class, () -> {
            authenticationService.login(loginDto);
        });

        assertEquals("Authentication failed: Invalid username or password.", exception.getMessage());
    }

    @Test
    public void testLoginWithDisabledUser() {

        LoginDto loginDto = new LoginDto("test@test.com","password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new DisabledException(""));

        DisabledUserException exception = assertThrows(DisabledUserException.class, () -> {
            authenticationService.login(loginDto);
        });

        assertEquals("This account is not activated yet.", exception.getMessage());
    }

    @Test
    public void testRegisterWithValidInput() {

        UserRequestDto userRequestDto = new UserRequestDto("testuser","test","test","null","test@test.com","password","password");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        User user = new User();
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<UserResponseDto> response = authenticationService.register(userRequestDto, bindingResult, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mailService, times(1)).sendConfirmationMail(any(User.class), eq(request));
    }

    @Test
    public void testOAuthLoginWithValidUser() {
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(authToken.getPrincipal()).thenReturn(oAuth2User);
        when(authToken.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(oAuth2User.getAttributes()).thenReturn(Map.of("email", "test@test.com"));

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("test-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        LoginResponse response = authenticationService.oAuthLogin(authToken);

        assertEquals("test-jwt-token", response.getToken());
        assertEquals(3600L, response.getExpiresIn());
    }

    @Test
    public void testRegisterWithInvalidPasswordMismatch() {
        UserRequestDto userRequestDto = new UserRequestDto("testuser","test","test","null","test@test.com","password","differentpassword");

        when(bindingResult.hasErrors()).thenReturn(false);

        PasswordsNotMatchingException exception = assertThrows(PasswordsNotMatchingException.class, () -> {
            authenticationService.register(userRequestDto, bindingResult, request);
        });

        assertEquals("Passwords don't match", exception.getMessage());
    }

    @Test
    public void testRegisterWithDuplicateEmail() {

        UserRequestDto userRequestDto = new UserRequestDto("testuser","test","test","null","test@test.com","password","password");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        EmailInUseException exception = assertThrows(EmailInUseException.class, () -> {
            authenticationService.register(userRequestDto, bindingResult, request);
        });

        assertEquals("Email already in use. Please use a different email", exception.getMessage());
    }

    @Test
    public void testConfirmEmailWithExpiredToken() {

        String token = "test-token";
        ConfirmationToken confirmationToken = mock(ConfirmationToken.class);
        when(confirmationTokenRepository.findByToken(anyString()))
                .thenReturn(Optional.of(confirmationToken));
        when(confirmationToken.getCreatedAt())
                .thenReturn(LocalDateTime.now().minusDays(5));
        when(confirmationToken.getUser())
                .thenReturn(new User());

        String result = authenticationService.confirmEmail(token);

        assertEquals("Expired token", result);
    }

    @Test
    public void testConfirmEmailWithValidToken() {

        String token = "test-token";
        User user = new User();
        user.setEnabled(false);
        ConfirmationToken confirmationToken = mock(ConfirmationToken.class);
        when(confirmationTokenRepository.findByToken(anyString()))
                .thenReturn(Optional.of(confirmationToken));
        when(confirmationToken.getCreatedAt())
                .thenReturn(LocalDateTime.now());
        when(confirmationToken.getUser())
                .thenReturn(user);

        String result = authenticationService.confirmEmail(token);

        assertEquals("Account activated successfully", result);
        assertTrue(user.isEnabled());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testResendConfirmationMailWhenUserIsNotActivated() {

        String email = "test@test.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        ConfirmationToken confirmationToken = new ConfirmationToken("test-token",LocalDateTime.now(),user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(confirmationTokenRepository.findByUser(user)).thenReturn(Optional.of(confirmationToken));

        String result = authenticationService.resendConfirmationMail(email, request);

        assertEquals("A new token was sent to your e-mail!", result);
        verify(confirmationTokenRepository).deleteById(confirmationToken.getId());
        verify(mailService).sendConfirmationMail(user, request);
    }

    @Test
    public void testResendConfirmationMailWhenUserAlreadyActivated() {

        String email = "test@test.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        String result = authenticationService.resendConfirmationMail(email, request);

        assertEquals("Account already activated", result);
        verify(confirmationTokenRepository, never()).findByUser(user);
        verify(mailService, never()).sendConfirmationMail(user, request);
    }

    @Test
    public void testResendConfirmationMailWhenUserNotExist() {

        String email = "test@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            authenticationService.resendConfirmationMail(email, request);
        });

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(confirmationTokenRepository, never()).findByUser(any());
        verify(mailService, never()).sendConfirmationMail(any(), eq(request));
    }

}

