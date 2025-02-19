package com.example.b2b_opportunities.Service.Implementation;

import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Service.Interface.UserService;
import com.example.b2b_opportunities.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getCurrentUserOrThrow(Authentication authentication) {
        if (authentication == null) {
            throw new AuthenticationFailedException("User not authenticated");
        }

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauthUser = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            String email = (String) oauthUser.getAttributes().get("email");
            return getUserByEmailOrThrow(email);
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // This will lazy load all company fields
        String email = userDetails.getUser().getEmail();
        return getUserByEmailOrThrow(email);
    }

    @Override
    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException("User with email: " + email + " not found"));
    }
}
