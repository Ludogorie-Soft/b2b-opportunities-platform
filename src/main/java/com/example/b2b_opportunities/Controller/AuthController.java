package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dto.Request.ResetPasswordDto;
import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Service.AuthenticationService;
import com.example.b2b_opportunities.Service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid UserRequestDto userRequestDto, BindingResult bindingResult, HttpServletRequest request) {
        return authenticationService.register(userRequestDto, bindingResult, request);
    }

    @GetMapping("/register/confirm")
    public String confirmEmail(@RequestParam("token") String token) {
        return authenticationService.confirmEmail(token);
    }

    @GetMapping("/register/resend-confirmation")
    @ResponseStatus(HttpStatus.OK)
    public String resendRegistrationMail(@RequestParam String email, HttpServletRequest request) {
        return authenticationService.resendConfirmationMail(email, request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public String login(@RequestBody LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
        return authenticationService.login(loginDto, request, response);
    }

    @GetMapping("/oauth2/success")
    @ResponseStatus(HttpStatus.OK)
    public void oAuthLogin(Principal user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.oAuthLogin(user, request, response);
        response.sendRedirect("/company/profile");
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

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request, HttpServletResponse response){
        authenticationService.logout(request, response);
    }
}
