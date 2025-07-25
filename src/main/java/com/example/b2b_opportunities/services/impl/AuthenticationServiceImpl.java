package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.dto.loginDtos.LoginDto;
import com.example.b2b_opportunities.dto.requestDtos.UserRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.UserResponseDto;
import com.example.b2b_opportunities.entity.ConfirmationToken;
import com.example.b2b_opportunities.entity.Role;
import com.example.b2b_opportunities.entity.User;
import com.example.b2b_opportunities.exception.AuthenticationFailedException;
import com.example.b2b_opportunities.exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.exception.common.DuplicateCredentialException;
import com.example.b2b_opportunities.exception.PasswordsNotMatchingException;
import com.example.b2b_opportunities.exception.ValidationException;
import com.example.b2b_opportunities.exception.common.InvalidRequestException;
import com.example.b2b_opportunities.mapper.UserMapper;
import com.example.b2b_opportunities.repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.repository.UserRepository;
import com.example.b2b_opportunities.services.interfaces.AuthenticationService;
import com.example.b2b_opportunities.services.interfaces.EmailDailyStatsService;
import com.example.b2b_opportunities.services.interfaces.JwtService;
import com.example.b2b_opportunities.services.interfaces.MailService;
import com.example.b2b_opportunities.services.interfaces.UserService;
import com.example.b2b_opportunities.enums.RoleType;
import com.example.b2b_opportunities.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

import static com.example.b2b_opportunities.utils.EmailUtils.validateEmail;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;
    private final UserService userService;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserRepository userRepository;
    private final EmailDailyStatsService emailDailyStatsService;

    @Value("${registration.token.expiration.time}")
    private int tokenExpirationDays;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value(("${domain}"))
    private String domain;

    @Override
    public void login(LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
        UserDetails userDetails;
        userDetails = authenticate(loginDto);
        log.info("User logged in using basic auth: {}", userDetails.getUsername());

        if (userDetails instanceof UserDetailsImpl userDetailsImpl) {
            User user = userDetailsImpl.getUser();
            setLastLogin(user);
        }

        String jwtToken = jwtService.generateToken(userDetails);

        setJwtCookie(request, response, jwtToken);
    }

    @Override
    public void setJwtCookie(HttpServletRequest request, HttpServletResponse response, String jwtToken) {

        ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtExpiration / 1000)
                .domain(domain)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public ResponseEntity<UserResponseDto> register(UserRequestDto userRequestDto, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        validateEmail(userRequestDto.getEmail());
        validateUser(userRequestDto);

        User user = UserMapper.toEntity(userRequestDto);
        userRepository.save(user);
        log.info("Registered new user: {}", userRequestDto.getUsername());
        mailService.sendConfirmationMail(user, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponseDto(user));
    }

    // TODO - Used only for testing
    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserMapper.toResponseDtoList(users);
    }

    @Override
    public String resendConfirmationMail(String email, HttpServletRequest request) {
        log.info("Attempting to send confirmation email to: {}", email);

        User user = userService.getUserByEmailOrThrow(email);

        if (user.isEnabled()) {
            log.info("Account with email: {} is already activated", email);
            throw new AlreadyExistsException("Account already activated", email);
        }
        Optional<ConfirmationToken> optionalToken = confirmationTokenRepository.findByUser(user);
        optionalToken.ifPresent(confirmationToken -> confirmationTokenRepository.deleteById(confirmationToken.getId()));
        mailService.sendConfirmationMail(user, request);
        return "A new token was sent to your e-mail!";
    }

    @Override
    public void oAuthLogin(Principal user, HttpServletRequest request, HttpServletResponse response) {
        if (user instanceof OAuth2AuthenticationToken authToken) {
            OAuth2User oauth2User = authToken.getPrincipal();

            String provider = authToken.getAuthorizedClientRegistrationId(); // google
            Map<String, Object> attributes = oauth2User.getAttributes();

            String email = (String) attributes.get("email");

            if (!isEmailInDB(email)) {
                log.info("Creating user using Oauth: {}", email);
                createUserFromOAuth(attributes, provider);
            }
            log.info("User logged in using Oauth: {}", email);
            setLastLogin(userService.getUserByEmailOrThrow(email));
            generateLoginResponse(request, response, email);
        }
    }

    // TODO - refactor - move to DB
    @Override
    public boolean isUsernameInDB(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    @Override
    public boolean arePasswordsMatching(String password, String repeatedPassword) {
        return password.equals(repeatedPassword);
    }

    @Override
    public ConfirmationToken validateAndReturnToken(String token) {
        Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenRepository.findByToken(token);
        if (optionalConfirmationToken.isEmpty()) {
            log.warn("Invalid token");
            throw new InvalidRequestException("Invalid token");
        }
        ConfirmationToken confirmationToken = optionalConfirmationToken.get();
        if (isTokenExpired(confirmationToken)) {
            log.warn("Token expired");
            throw new InvalidRequestException("Expired token");
        }
        return optionalConfirmationToken.get();
    }

    @Override
    public void confirmEmail(String token) {
        ConfirmationToken confirmationToken = validateAndReturnToken(token);
        User user = confirmationToken.getUser();
        user.setEnabled(true);
        confirmationTokenRepository.delete(confirmationToken);
        log.info("Email: {} for user: {} confirmed using a token", user.getEmail(), user.getUsername());
        userRepository.save(user);
        emailDailyStatsService.incrementActivationMailsOpened();
    }


//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            Arrays.stream(cookies).forEach(cookie -> {
//                if ("jwt".equals(cookie.getName())) {
//                    cookie.setValue(null);
//                    cookie.setMaxAge(0);
//                    cookie.setPath("/");
//                    cookie.setDomain("b2bapp.algorithmithy.com");
//                    cookie.setHttpOnly(true);
//                    response.addCookie(cookie);
//                    response.setHeader("Set-Cookie", "jwt=; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=0; Domain=b2bapp.algorithmithy.com");
//                }
//            });
//        }
//    public void logout(HttpServletRequest request, HttpServletResponse response) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            Arrays.stream(cookies).forEach(cookie -> {
//                if ("jwt".equals(cookie.getName())) {
//                    ResponseCookie responseCookie = ResponseCookie.from("jwt", "")
//                            .path("/")
//                            .domain("b2bapp.algorithmithy.com")
//                            .httpOnly(true)
//                            .secure(true)
//                            .sameSite("None")
//                            .maxAge(0)
//                            .build();
//
//                    response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
//                }
//            });
//        }
//    }

    private UserDetails authenticate(LoginDto loginDto) {
        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail().toLowerCase(), loginDto.getPassword());
            Authentication authResult = authenticationManager.authenticate(authentication);
            return (UserDetailsImpl) authResult.getPrincipal();
        } catch (DisabledException e) {
            log.warn("Authentication failed for user/email {}. Account not activated yet.", loginDto.getUsernameOrEmail());
            throw new AuthenticationFailedException("This account is not activated yet."); // TODO: email confirmation not accepted
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user/email {}. Invalid username or password.", loginDto.getUsernameOrEmail());
            throw new AuthenticationFailedException("Authentication failed: Invalid username or password.");
        }
    }

    private void validateUser(UserRequestDto userRequestDto) {
        if (isEmailInDB(userRequestDto.getEmail().toLowerCase())) {
            log.warn("Email {} already in use. User {} needs to try a different email.", userRequestDto.getEmail(), userRequestDto.getUsername());
            throw new DuplicateCredentialException("Email already in use. Please use a different email", "email");
        }
        if (isUsernameInDB(userRequestDto.getUsername().toLowerCase())) {
            log.warn("Username {} already in use.", userRequestDto.getUsername());
            throw new DuplicateCredentialException("Username already in use. Please use a different username", "username");
        }
        if (!arePasswordsMatching(userRequestDto.getPassword(), userRequestDto.getRepeatedPassword())) {
            log.warn("Passwords don't match for user: {}", userRequestDto.getUsername());
            throw new PasswordsNotMatchingException("Passwords don't match", "password");
        }
    }

    // TODO - refactor - move to DB
    private boolean isEmailInDB(String email) {
        return userRepository.findByEmail(email).isPresent();
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

    private void generateLoginResponse(HttpServletRequest request, HttpServletResponse response, String email) {
        User user = userService.getUserByEmailOrThrow(email);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String jwtToken = jwtService.generateToken(userDetails);

        setJwtCookie(request, response, jwtToken);
    }

    private boolean isTokenExpired(ConfirmationToken token) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Duration duration = Duration.between(token.getCreatedAt(), currentDateTime);
        return duration.toDays() > tokenExpirationDays;
    }

    private void setLastLogin(User user){
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}