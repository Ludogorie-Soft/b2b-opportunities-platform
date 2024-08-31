package com.example.b2b_opportunities.Dtos.LoginDtos;

import lombok.*;

@Builder
@Getter
@Setter
public class LoginResponse {
    private String token;
    private long expiresIn;
}
