package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Service.UserService;
import com.example.b2b_opportunities.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public UserResponseDto getUserDetails(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationFailedException("Not authenticated");
        }

        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return UserMapper.toResponseDto(userDetails.getUser());
        }
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauthUser = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            String email = (String) oauthUser.getAttributes().get("email");
            return UserMapper.toResponseDto(userService.getUserByEmailOrThrow(email));
        }
        throw new IllegalStateException("Unsupported authentication type");
    }
}
