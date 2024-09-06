package com.example.b2b_opportunities.Dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String companyName;
    private LocalDateTime createdAt;
    private boolean isEnabled;
}
