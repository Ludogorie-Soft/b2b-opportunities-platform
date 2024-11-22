package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.Role;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.DuplicateCredentialException;
import com.example.b2b_opportunities.Exception.PasswordsNotMatchingException;
import com.example.b2b_opportunities.Exception.ValidationException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.RoleType;
import com.example.b2b_opportunities.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.b2b_opportunities.Utils.EmailUtils.validateEmail;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;
    private final UserService userService;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserRepository userRepository;

    @Value("${registration.token.expiration.time}")
    private int tokenExpirationDays;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value(("${domain}"))
    private String domain;

    public void login(LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
        UserDetails userDetails;
        userDetails = authenticate(loginDto);
        log.info("User logged in using basic auth: {}", userDetails.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);

        setJwtCookie(request, response, jwtToken);
    }

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
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserMapper.toResponseDtoList(users);
    }

    public String resendConfirmationMail(String email, HttpServletRequest request) {
        log.info("Attempting to send confirmation email to: {}", email);

        User user = userService.getUserByEmailOrThrow(email);

        if (user.isEnabled()) {
            log.info("Account with email: {} is already activated", email);
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
            generateLoginResponse(request, response, email);
        }
    }

    // TODO - refactor - move to DB
    public boolean isUsernameInDB(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean arePasswordsMatching(String password, String repeatedPassword) {
        return password.equals(repeatedPassword);
    }

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

    public void confirmEmail(String token) {
        ConfirmationToken confirmationToken = validateAndReturnToken(token);
        User user = confirmationToken.getUser();
        user.setEnabled(true);
        log.info("Email: {} for user: {} confirmed using a token", user.getEmail(), user.getUsername());
        userRepository.save(user);
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
            throw new DuplicateCredentialException("Email already in use. Please use a different email");
        }
        if (isUsernameInDB(userRequestDto.getUsername().toLowerCase())) {
            log.warn("Username {} already in use.", userRequestDto.getUsername());
            throw new DuplicateCredentialException("Username already in use. Please use a different username");
        }
        if (!arePasswordsMatching(userRequestDto.getPassword(), userRequestDto.getRepeatedPassword())) {
            log.warn("Passwords don't match for user: {}", userRequestDto.getUsername());
            throw new PasswordsNotMatchingException("Passwords don't match");
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
}