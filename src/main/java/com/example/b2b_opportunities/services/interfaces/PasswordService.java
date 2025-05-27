package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.dto.requestDtos.ResetPasswordDto;
import jakarta.servlet.http.HttpServletRequest;

public interface PasswordService {
    String requestPasswordRecovery(String email, HttpServletRequest request);

    String setNewPassword(ResetPasswordDto resetPasswordDto);
}
