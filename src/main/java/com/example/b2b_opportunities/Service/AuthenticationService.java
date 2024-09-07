package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dtos.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dtos.LoginDtos.LoginResponse;
import com.example.b2b_opportunities.Dtos.Request.UserRequestDto;
import com.example.b2b_opportunities.Dtos.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.Role;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exceptions.*;
import com.example.b2b_opportunities.Mappers.UserMapper;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.RoleType;
import com.example.b2b_opportunities.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ResponseEntity<LoginResponse> login(LoginDto loginDto) {
        UserDetails userDetails;
        userDetails = authenticate(loginDto);

        String jwtToken = jwtService.generateToken(userDetails);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity.ok(loginResponse);
    }


    private UserDetails authenticate(LoginDto loginDto) {
        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail().toLowerCase(), loginDto.getPassword());
            Authentication authResult = authenticationManager.authenticate(authentication);
            return (UserDetailsImpl) authResult.getPrincipal();
        } catch (DisabledException e) {
            throw new DisabledUserException("This account is not activated yet."); // TODO: email confirmation not accepted
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException("Authentication failed: Invalid username or password.");
        }
    }

    public ResponseEntity<UserResponseDto> register(UserRequestDto userRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        validateUser(userRequestDto);

        User user = UserMapper.toDto(userRequestDto);

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(user));
    }

    private void validateUser(UserRequestDto userRequestDto) {
        if (isEmailInDB(userRequestDto.getEmail())) {
            throw new EmailInUseException("Email already in use. Please use a different email");
        }
        if (isUsernameInDB(userRequestDto.getUsername())) {
            throw new UsernameInUseException("Username already in use. Please use a different username");
        }
        if (!arePasswordsMatching(userRequestDto)) {
            throw new PasswordsNotMatchingException("Passwords don't match");
        }
    }

    private boolean isEmailInDB(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private boolean isUsernameInDB(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private boolean arePasswordsMatching(UserRequestDto userRequestDto) {
        return userRequestDto.getPassword().equals(userRequestDto.getRepeatedPassword());
    }

    public LoginResponse oAuthLogin(Principal user) {
        if (user instanceof OAuth2AuthenticationToken authToken) {
            OAuth2User oauth2User = authToken.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();

            String email = (String) attributes.get("email");

            if (!isEmailInDB(email)) {
                createUserFromOAuth(attributes);
            }

            return generateLoginResponse(email);
        }
        throw new ServerErrorException("Authentication failed: The provided authentication is not an OAuth2 token.");
    }

    private void createUserFromOAuth(Map<String, Object> attributes) {
        RoleType roleUser = RoleType.ROLE_USER;
        Role role = Role.builder()
                .id(roleUser.getId())
                .name(roleUser.name())
                .build();

        String email = (String) attributes.get("email");
        User user = User.builder()
                .username(email)
                .firstName((String) attributes.get("given_name"))
                .lastName((String) attributes.get("family_name"))
                .email(email)
                .provider("google")
                .createdAt(LocalDateTime.now())
                .isEnabled(true)
                .role(role)
                .build();

        userRepository.save(user);
    }

    private LoginResponse generateLoginResponse(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        return LoginResponse.builder()
                .token(jwtService.generateToken(userDetails))
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
}
