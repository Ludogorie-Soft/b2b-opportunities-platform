package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dtos.Request.EmployerRequestDto;
import com.example.b2b_opportunities.Dtos.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dtos.LoginDtos.LoginResponse;
import com.example.b2b_opportunities.Dtos.Response.EmployerResponseDto;
import com.example.b2b_opportunities.Entity.Employer;
import com.example.b2b_opportunities.MyUserDetails;
import com.example.b2b_opportunities.Exceptions.*;
import com.example.b2b_opportunities.Mappers.EmployerMapper;
import com.example.b2b_opportunities.Repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmployerRepository employerRepository;
    private final BCryptPasswordEncoder encoder;


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
            Authentication authentication = new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword());
            Authentication authResult = authenticationManager.authenticate(authentication);
            return (MyUserDetails) authResult.getPrincipal();
        } catch (DisabledException e) {
            throw new DisabledEmployerException("This account is not activated yet."); // TODO: email confirmation not accepted
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException("Authentication failed: Invalid username or password.");
        }
    }

    public ResponseEntity<EmployerResponseDto> register(EmployerRequestDto employerRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        validateEmployer(employerRequestDto);

        Employer employer = EmployerMapper.toDto(employerRequestDto);

        employerRepository.save(employer);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployerMapper.toResponse(employer));
    }

    private void validateEmployer(EmployerRequestDto employerRequestDto) {
        if (isEmailInDB(employerRequestDto.getEmail())) {
            throw new EmailInUseException("Email already in use. Please use a different email");
        }
        if (isUsernameInDB(employerRequestDto.getUsername())) {
            throw new UsernameInUseException("Username already in use. Please use a different username");
        }
        if (!arePasswordsMatching(employerRequestDto)) {
            throw new PasswordsNotMatchingException("Passwords don't match");
        }
    }

    private boolean isEmailInDB(String email) {
        return employerRepository.findByEmail(email).isPresent();
    }

    private boolean isUsernameInDB(String username) {
        return employerRepository.findByUsername(username).isPresent();
    }

    private boolean arePasswordsMatching(EmployerRequestDto employerRequestDto) {
        return employerRequestDto.getPassword().equals(employerRequestDto.getRepeatedPassword());
    }

}
