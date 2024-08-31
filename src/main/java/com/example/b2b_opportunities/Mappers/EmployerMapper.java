package com.example.b2b_opportunities.Mappers;

import com.example.b2b_opportunities.Configs.SecurityConfig;
import com.example.b2b_opportunities.Dtos.Request.EmployerRequestDto;
import com.example.b2b_opportunities.Dtos.Response.EmployerResponseDto;
import com.example.b2b_opportunities.Entity.Employer;
import com.example.b2b_opportunities.Entity.Role;
import com.example.b2b_opportunities.Static.RoleType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EmployerMapper {

    public static Employer toDto(EmployerRequestDto employerRequestDto) {

        // Setting up the role without checking in the DB
        RoleType roleEmployer = RoleType.ROLE_EMPLOYER;

        Role role = Role.builder()
                .id(roleEmployer.getId())
                .name(roleEmployer.name())
                .build();

        return Employer.builder()
                .username(employerRequestDto.getUsername())
                .email(employerRequestDto.getEmail())
                .password(SecurityConfig.passwordEncoder().encode(employerRequestDto.getPassword()))
                .companyName(employerRequestDto.getCompanyName())
                .role(role)
                .createdAt(LocalDateTime.now())
                .isEnabled(true)
                .build();
    }

    public static EmployerResponseDto toResponse(Employer employer) {
        return EmployerResponseDto.builder()
                .id(employer.getId())
                .username(employer.getUsername())
                .email(employer.getEmail())
                .companyName(employer.getCompanyName())
                .createdAt(employer.getCreatedAt())
                .isEnabled(employer.isEnabled())
                .build();
    }
}
