package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dto.LoginDtos.LoginResponse;
import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginDto loginDto) {
        return authenticationService.login(loginDto);
    }
}
