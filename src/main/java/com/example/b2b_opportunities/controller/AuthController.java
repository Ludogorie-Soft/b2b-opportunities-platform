package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.dto.loginDtos.LoginDto;
import com.example.b2b_opportunities.dto.requestDtos.ResetPasswordDto;
import com.example.b2b_opportunities.dto.requestDtos.UserRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.UserResponseDto;
import com.example.b2b_opportunities.exception.AuthenticationFailedException;
import com.example.b2b_opportunities.services.interfaces.AuthenticationService;
import com.example.b2b_opportunities.services.interfaces.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final PasswordService passwordService;
    @Value("${frontend.address}")
    private String frontEndAddress;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid UserRequestDto userRequestDto,
                                                    BindingResult bindingResult,
                                                    HttpServletRequest request) {
        return authenticationService.register(userRequestDto, bindingResult, request);
//        response.sendRedirect("http://localhost:5173/signup?confirmEmail=true");
    }

    @GetMapping("/register/confirm")
    public void confirmEmail(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        authenticationService.confirmEmail(token);
        // TODO - change localhost to use env var
        response.sendRedirect(frontEndAddress + "/signup?confirmEmail=true");
    }

    @GetMapping("/register/resend-confirmation")
    @ResponseStatus(HttpStatus.OK)
    public String resendRegistrationMail(@RequestParam String email, HttpServletRequest request) {
        return authenticationService.resendConfirmationMail(email, request);
    }

    @PostMapping("/login")
    public void login(@RequestBody LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
        authenticationService.login(loginDto, request, response);
    }

    @GetMapping("/oauth2/success")
    public void oAuthLogin(Principal user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.oAuthLogin(user, request, response);
        response.sendRedirect(frontEndAddress + "/company/profile");
    }

    // Just for testing - returns the user details from Google after oAuth
    @GetMapping("/user")
    public Principal getUserDetails(Principal user) {
        if (user == null) {
            throw new AuthenticationFailedException("Not authenticated");
        }
        return user;
    }

    @GetMapping("/password-recovery")
    public String requestPasswordRecovery(@RequestParam String email, HttpServletRequest request) {
        return passwordService.requestPasswordRecovery(email, request);
    }

    @PostMapping("/set-new-password")
    @ResponseStatus(HttpStatus.OK)
    public String changePassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        return passwordService.setNewPassword(resetPasswordDto);
    }

//    @PostMapping("/logout")
//    @ResponseStatus(HttpStatus.OK)
//    public void logout(HttpServletRequest request, HttpServletResponse response) {
//        authenticationService.logout(request, response);
//    }
}
