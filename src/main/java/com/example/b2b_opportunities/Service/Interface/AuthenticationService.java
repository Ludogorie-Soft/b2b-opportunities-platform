package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Dto.LoginDtos.LoginDto;
import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.util.List;

public interface AuthenticationService {
    void login(LoginDto loginDto, HttpServletRequest request, HttpServletResponse response);

    void setJwtCookie(HttpServletRequest request, HttpServletResponse response, String jwtToken);

    ResponseEntity<UserResponseDto> register(UserRequestDto userRequestDto, BindingResult bindingResult, HttpServletRequest request);

    List<UserResponseDto> getAllUsers();

    String resendConfirmationMail(String email, HttpServletRequest request);

    void oAuthLogin(Principal user, HttpServletRequest request, HttpServletResponse response);

    boolean isUsernameInDB(String username);

    boolean arePasswordsMatching(String password, String repeatedPassword);

    ConfirmationToken validateAndReturnToken(String token);

    void confirmEmail(String token);
}