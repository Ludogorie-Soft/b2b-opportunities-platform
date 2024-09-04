package com.example.b2b_opportunities.Mappers;

import com.example.b2b_opportunities.Configs.SecurityConfig;
import com.example.b2b_opportunities.Dtos.Request.UserRequestDto;
import com.example.b2b_opportunities.Dtos.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Entity.Role;
import com.example.b2b_opportunities.Static.RoleType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public static User toDto(UserRequestDto userRequestDto) {

        // Setting up the role without checking in the DB
        RoleType roleUser = RoleType.ROLE_USER;

        Role role = Role.builder()
                .id(roleUser.getId())
                .name(roleUser.name())
                .build();

        return User.builder()
                .firstName(userRequestDto.getFirstName())
                .lastName(userRequestDto.getLastName())
                .username(userRequestDto.getUsername().toLowerCase())
                .email(userRequestDto.getEmail().toLowerCase())
                .password(SecurityConfig.passwordEncoder().encode(userRequestDto.getPassword()))
                .companyName(userRequestDto.getCompanyName())
                .role(role)
                .createdAt(LocalDateTime.now())
                .isEnabled(false)
                .build();
    }

    public static UserResponseDto toResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .companyName(user.getCompanyName())
                .createdAt(user.getCreatedAt())
                .isEnabled(user.isEnabled())
                .build();
    }
}
