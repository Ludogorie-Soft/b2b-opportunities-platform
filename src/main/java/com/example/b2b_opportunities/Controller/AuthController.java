package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dtos.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dtos.LoginDtos.LoginResponse;
import com.example.b2b_opportunities.Dtos.Request.UserRequestDto;
import com.example.b2b_opportunities.Dtos.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid UserRequestDto userRequestDto, BindingResult bindingResult) {
        return authenticationService.register(userRequestDto, bindingResult);
    }

    @GetMapping("/register/confirm")
    public String confirmEmail(@RequestParam("token") String token) {
        return authenticationService.confirmEmail(token);
    }

    @GetMapping("/register/resend-confirmation")
    @ResponseStatus(HttpStatus.OK)
    public String resendRegistrationMail(@RequestParam String email) {
        return authenticationService.resendConfirmationMail(email);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginDto loginDto) {
        return authenticationService.login(loginDto);
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<LoginResponse> oauth2LoginSuccess(OAuth2AuthenticationToken authentication) {
        return authenticationService.processOAuthPostLogin(authentication);
    }
}
