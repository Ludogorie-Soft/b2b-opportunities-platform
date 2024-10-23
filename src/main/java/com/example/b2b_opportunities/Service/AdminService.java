package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

    public UserResponseDto approve(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        if (user.isApproved()) {
            return UserMapper.toResponseDto(user);
        }
        user.setApproved(true);
        return UserMapper.toResponseDto(userRepository.save(user));
    }

    public List<UserResponseDto> getAllNonApprovedUsers() {
        List<User> users = userRepository.findByIsApprovedFalse();
        return UserMapper.toResponseDtoList(users);
    }

    // TODO: move to a different location ?
    public User getCurrentUserOrThrow(Authentication authentication) {
        if (authentication == null) {
            throw new AuthenticationFailedException("User not authenticated");
        }

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauthUser = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            String email = (String) oauthUser.getAttributes().get("email");
            return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User with email: " + email + " not found"));
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // This will lazy load all company fields
        String email = userDetails.getUser().getEmail();
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User with email: " + email + " not found"));
    }
}
