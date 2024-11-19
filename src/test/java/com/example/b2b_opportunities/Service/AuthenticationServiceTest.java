package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.DuplicateCredentialException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.PasswordsNotMatchingException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {
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

    @Mock
    private HttpServletResponse response;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void testLoginWithValidInput() {
        LoginDto loginDto = new LoginDto("test@test.com", "password");
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

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        authenticationService.login(loginDto, request, response);

        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    void testLoginWithWrongPassword() {
        LoginDto loginDto = new LoginDto("test@test.com", "wrong-password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new AuthenticationException("") {
                });

        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class, () -> authenticationService.login(loginDto, request, response)
        );

        assertEquals("Authentication failed: Invalid username or password.", exception.getMessage());
    }

    @Test
    void testLoginWithDisabledUser() {
        LoginDto loginDto = new LoginDto("test@test.com", "password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new DisabledException(""));

        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class, () -> {
            authenticationService.login(loginDto, request, response);
        });

        assertEquals("This account is not activated yet.", exception.getMessage());
    }

    @Test
    void testRegisterWithValidInput() throws IOException {
        UserRequestDto userRequestDto = new UserRequestDto("testuser", "test", "test", "test@test.com", "password", "password");

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
    void testOAuthLoginWithValidUser() {
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(authToken.getPrincipal()).thenReturn(oAuth2User);
        when(authToken.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(oAuth2User.getAttributes()).thenReturn(Map.of("email", "test@test.com"));

        User existingUser = new User();
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
        existingUser.setEnabled(true);
        existingUser.setProvider("google");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("test-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        authenticationService.oAuthLogin(authToken, request, response);

        verify(jwtService).generateToken(any(UserDetails.class));
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    void testRegisterWithInvalidPasswordMismatch() {
        UserRequestDto userRequestDto = new UserRequestDto("testuser", "test", "test", "test@test.com", "password", "differentpassword");

        when(bindingResult.hasErrors()).thenReturn(false);

        PasswordsNotMatchingException exception = assertThrows(PasswordsNotMatchingException.class, () -> {
            authenticationService.register(userRequestDto, bindingResult, request);
        });

        assertEquals("Passwords don't match", exception.getMessage());
    }

    @Test
    void testRegisterWithDuplicateEmail() {
        UserRequestDto userRequestDto = new UserRequestDto("testuser", "test", "test", "test@test.com", "password", "password");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        DuplicateCredentialException exception = assertThrows(DuplicateCredentialException.class, () -> {
            authenticationService.register(userRequestDto, bindingResult, request);
        });

        assertEquals("Email already in use. Please use a different email", exception.getMessage());
    }

    @Test
    void testConfirmEmailWithExpiredToken() {
        String token = "test-token";
        ConfirmationToken confirmationToken = mock(ConfirmationToken.class);
        when(confirmationTokenRepository.findByToken(anyString()))
                .thenReturn(Optional.of(confirmationToken));
        when(confirmationToken.getCreatedAt())
                .thenReturn(LocalDateTime.now().minusDays(5));
        when(confirmationToken.getUser())
                .thenReturn(new User());

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authenticationService.confirmEmail(token);
        });

        assertEquals("Expired token", exception.getMessage());
    }

    @Test
    void testConfirmEmailWithValidToken() {
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

        authenticationService.confirmEmail(token);

//        assertEquals("Account activated successfully", result);
        assertTrue(user.isEnabled());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testResendConfirmationMailWhenUserIsNotActivated() {
        String email = "test@test.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        ConfirmationToken confirmationToken = new ConfirmationToken("test-token", user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(confirmationTokenRepository.findByUser(user)).thenReturn(Optional.of(confirmationToken));

        String result = authenticationService.resendConfirmationMail(email, request);

        assertEquals("A new token was sent to your e-mail!", result);
        verify(confirmationTokenRepository).deleteById(confirmationToken.getId());
        verify(mailService).sendConfirmationMail(user, request);
    }

    @Test
    void testResendConfirmationMailWhenUserAlreadyActivated() {
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
    void testResendConfirmationMailWhenUserNotExist() {
        String email = "test@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            authenticationService.resendConfirmationMail(email, request);
        });

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(confirmationTokenRepository, never()).findByUser(any());
        verify(mailService, never()).sendConfirmationMail(any(), eq(request));
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        List<User> users = Arrays.asList(user1, user2);
        List<UserResponseDto> expectedResponse = Arrays.asList(
                UserMapper.toResponseDto(user1),
                UserMapper.toResponseDto(user2)
        );

        when(userRepository.findAll()).thenReturn(users);

        try (var mockedUserMapper = mockStatic(UserMapper.class)) {
            mockedUserMapper.when(() -> UserMapper.toResponseDtoList(users)).thenReturn(expectedResponse);

            List<UserResponseDto> actualResponse = authenticationService.getAllUsers();

            verify(userRepository, times(1)).findAll();

            mockedUserMapper.verify(() -> UserMapper.toResponseDtoList(users), times(1));

            assertEquals(expectedResponse, actualResponse);
            assertEquals(2, actualResponse.size());
        }
    }
}