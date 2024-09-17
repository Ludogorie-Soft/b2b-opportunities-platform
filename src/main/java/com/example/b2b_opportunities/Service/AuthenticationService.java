package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dto.LoginDtos.LoginResponse;
import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.Role;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.DisabledUserException;
import com.example.b2b_opportunities.Exception.EmailInUseException;
import com.example.b2b_opportunities.Exception.PasswordsNotMatchingException;
import com.example.b2b_opportunities.Exception.ServerErrorException;
import com.example.b2b_opportunities.Exception.UserNotFoundException;
import com.example.b2b_opportunities.Exception.UsernameInUseException;
import com.example.b2b_opportunities.Exception.ValidationException;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.RoleType;
import com.example.b2b_opportunities.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;

    @Value("${registration.token.expiration.time}")
    private int tokenExpirationDays;
    private final ConfirmationTokenRepository confirmationTokenRepository;
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

    public ResponseEntity<UserResponseDto> register(UserRequestDto userRequestDto, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        validateUser(userRequestDto);

        User user = UserMapper.toEntity(userRequestDto);
        userRepository.save(user);
        mailService.sendConfirmationMail(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponseDto(user));
    }

    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserMapper.toResponseDtoList(users);
    }

    public String confirmEmail(String token) {
        Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenRepository.findByToken(token);
        if (optionalConfirmationToken.isEmpty()) {
            return "Invalid token"; //TODO - this will be improved
        }
        ConfirmationToken confirmationToken = optionalConfirmationToken.get();
        if (isTokenExpired(confirmationToken)) {
            return "Expired token"; //TODO - this will be improved -> resend?
        }
        User user = confirmationToken.getUser();
        if (user.isEnabled()) {
            return "Account already activated";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "Account activated successfully";
    }

    public String resendConfirmationMail(String email, HttpServletRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(()
                -> new UserNotFoundException("User not found with email: " + email));
        if (user.isEnabled()) {
            return "Account already activated";
        }
        Optional<ConfirmationToken> optionalToken = confirmationTokenRepository.findByUser(user);
        if (optionalToken.isPresent()) {
            ConfirmationToken confirmationToken = optionalToken.get();
            confirmationTokenRepository.deleteById(confirmationToken.getId());
        }
        mailService.sendConfirmationMail(user, request);
        return "A new token was sent to your e-mail!";
    }

    public LoginResponse oAuthLogin(Principal user) {
        if (user instanceof OAuth2AuthenticationToken authToken) {
            OAuth2User oauth2User = authToken.getPrincipal();

            String provider = authToken.getAuthorizedClientRegistrationId(); // google
            Map<String, Object> attributes = oauth2User.getAttributes();

            String email = (String) attributes.get("email");

            if (!isEmailInDB(email)) {
                createUserFromOAuth(attributes, provider);
            }

            return generateLoginResponse(email);
        }
        throw new ServerErrorException("Authentication failed: The provided authentication is not an OAuth2 token.");
    }

    public boolean isUsernameInDB(String username) {
        return userRepository.findByUsername(username).isPresent();
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

    private void validateUser(UserRequestDto userRequestDto) {
        if (isEmailInDB(userRequestDto.getEmail().toLowerCase())) {
            throw new EmailInUseException("Email already in use. Please use a different email");
        }
        if (isUsernameInDB(userRequestDto.getUsername().toLowerCase())) {
            throw new UsernameInUseException("Username already in use. Please use a different username");
        }
        if (!arePasswordsMatching(userRequestDto)) {
            throw new PasswordsNotMatchingException("Passwords don't match");
        }
    }

    private boolean isEmailInDB(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private boolean arePasswordsMatching(UserRequestDto userRequestDto) {
        return userRequestDto.getPassword().equals(userRequestDto.getRepeatedPassword());
    }

    private void createUserFromOAuth(Map<String, Object> attributes, String provider) {
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
                .provider(provider)
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

    private boolean isTokenExpired(ConfirmationToken token) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Duration duration = Duration.between(token.getCreatedAt(), currentDateTime);
        return duration.toDays() > tokenExpirationDays;
    }
}