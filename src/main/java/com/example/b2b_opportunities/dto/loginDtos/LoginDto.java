package com.example.b2b_opportunities.dto.loginDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginDto {
    private String usernameOrEmail;
    private String password;
}
