package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Dto.Request.ResetPasswordDto;
import jakarta.servlet.http.HttpServletRequest;

public interface PasswordService {
    String requestPasswordRecovery(String email, HttpServletRequest request);

    String setNewPassword(ResetPasswordDto resetPasswordDto);
}
