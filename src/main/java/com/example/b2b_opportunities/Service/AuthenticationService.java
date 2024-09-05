package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dtos.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dtos.LoginDtos.LoginResponse;
import com.example.b2b_opportunities.Dtos.Request.UserRequestDto;
import com.example.b2b_opportunities.Dtos.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.Role;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exceptions.*;
import com.example.b2b_opportunities.Mappers.UserMapper;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.RoleType;
import com.example.b2b_opportunities.UserDetailsImpl;
import jakarta.mail.MessagingException;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
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
        mailService.sendConfirmationMail(user);
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

    private boolean isTokenExpired(ConfirmationToken token) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Duration duration = Duration.between(token.getCreatedAt(), currentDateTime);
        return duration.toDays() > tokenExpirationDays;
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

    public String resendConfirmationMail(String email) {
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
        mailService.sendConfirmationMail(user);
        return "A new token was sent to your e-mail!";
    }

    public ResponseEntity<LoginResponse> processOAuthPostLogin(OAuth2AuthenticationToken authentication) {
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String providerId = (String) attributes.get("sub");

        User user;
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            RoleType roleUser = RoleType.ROLE_USER;
            Role role = Role.builder().id(roleUser.getId()).name(roleUser.name()).build();
            user = User.builder()
                    .username(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .provider("google")
                    .providerId(providerId)
                    .createdAt(LocalDateTime.now())
                    .isEnabled(true)
                    .role(role)
                    .build();
            user = userRepository.save(user);
        } else {
            user = existingUser.get();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user = userRepository.save(user); // Update names if they are changed in Google Account
        }
        UserDetails userDetails = new UserDetailsImpl(user);
        String token = jwtService.generateToken(userDetails);

        long expiresIn = jwtService.getExpirationTime();

        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
        return ResponseEntity.ok(loginResponse);
    }
}