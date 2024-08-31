package com.example.b2b_opportunities.Dtos.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EmployerResponseDto {
    private Long id;
    private String username;
    private String email;
    private String companyName;
    private LocalDateTime createdAt;
    private boolean isEnabled;
}
