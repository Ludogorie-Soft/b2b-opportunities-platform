package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Config.SecurityConfig;
import com.example.b2b_opportunities.Dto.Request.UserRequestDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Role;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Static.RoleType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserMapper {

    public static User toEntity(UserRequestDto userRequestDto) {

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
                .role(role)
                .createdAt(LocalDateTime.now())
                .isEnabled(false)
                .isApproved(false)
                .build();
    }

    public static UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .isEnabled(user.isEnabled())
                .isApproved(user.isApproved())
                .companyId((Optional.ofNullable(user.getCompany())
                        .map(Company::getId)
                        .orElse(null)))
                .build();
    }

    public static List<UserResponseDto> toResponseDtoList(List<User> users) {
        List<UserResponseDto> userResponseDtoList = new ArrayList<>();
        for (User user : users) {
            userResponseDtoList.add(toResponseDto(user));
        }
        return userResponseDtoList;
    }
}
