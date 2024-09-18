package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Config.SecurityConfig;
import com.example.b2b_opportunities.Dto.Request.ResetPasswordDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.DisabledUserException;
import com.example.b2b_opportunities.Exception.PasswordsNotMatchingException;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final AuthenticationService authenticationService;
    private final MailService mailService;
    private final UserRepository userRepository;

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public String requestPasswordRecovery(String email, HttpServletRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NullPointerException("User not registered"));
        if (!user.isEnabled()) {
            throw new DisabledUserException("Account not activated");
        }
        mailService.sendPasswordRecoveryMail(user, request);
        return "Password recovery e-mail was sent successfully";
    }

    public String generatePasswordRecoveryToken(User user) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken recoveryToken = new ConfirmationToken(
                token,
                user
        );
        confirmationTokenRepository.save(recoveryToken);
        return token;
    }

    public String setNewPassword(ResetPasswordDto resetPasswordDto) {
        ConfirmationToken confirmationToken = authenticationService.validateAndReturnToken(resetPasswordDto.getToken());
        User user = confirmationToken.getUser();
        if (authenticationService.arePasswordsMatching(
                resetPasswordDto.getNewPassword(),
                resetPasswordDto.getRepeatPassword())) {
            user.setPassword(SecurityConfig.passwordEncoder().encode(resetPasswordDto.getNewPassword()));
            userRepository.save(user);
            return "Password changed successfully";
        }
        throw new PasswordsNotMatchingException("Passwords do not match");
    }
}
