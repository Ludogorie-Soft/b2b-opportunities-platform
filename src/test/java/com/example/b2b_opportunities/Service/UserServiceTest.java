package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void whenAuthenticationIsNullGetCurrentUserOrThrowThrowsAuthenticationFailedException() {
        assertThrows(AuthenticationFailedException.class,
                () -> userService.getCurrentUserOrThrow(null));
    }

    @Test
    void whenGetCurrentUserOrThrowReturnsUserWhenOAuth2AuthenticationToken() {
        String email = "test@example.com";
        Map<String, Object> attributes = Map.of("email", email);
        OAuth2User oauthUser = mock(OAuth2User.class);
        when(oauthUser.getAttributes()).thenReturn(attributes);
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getPrincipal()).thenReturn(oauthUser);

        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getCurrentUserOrThrow(authentication);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void whenGetCurrentUserOrThrowShouldReturnUserWhenUserDetailsImpl() {
        String email = "test@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUser()).thenReturn(mockUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        User result = userService.getCurrentUserOrThrow(authentication);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void whenGetCurrentUserOrThrowShouldThrowNotFoundExceptionWhenUserNotFound() {
        String email = "test@example.com";
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        User mockUser = new User();
        mockUser.setEmail(email);
        when(userDetails.getUser()).thenReturn(mockUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.getCurrentUserOrThrow(authentication));
    }

    @Test
    void whenGetUserByEmailOrThrowShouldReturnUserWhenUserExists() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getUserByEmailOrThrow(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void whenGetUserByEmailOrThrowShouldThrowNotFoundExceptionWhenUserNotFound() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.getUserByEmailOrThrow(email));
    }
}