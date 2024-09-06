package com.example.b2b_opportunities.Dto.LoginDtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {
    private String usernameOrEmail;
    private String password;
}
