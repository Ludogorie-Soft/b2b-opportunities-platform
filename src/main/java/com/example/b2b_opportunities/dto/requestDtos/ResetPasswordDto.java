package com.example.b2b_opportunities.dto.requestDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResetPasswordDto {
    String token;
    String newPassword;
    String repeatPassword;
}