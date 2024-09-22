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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final MailService mailService;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    public String requestPasswordRecovery(String email, HttpServletRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NullPointerException("User not registered"));
        if (!user.isEnabled()) {
            throw new DisabledUserException("Account not activated");
        }
        mailService.sendPasswordRecoveryMail(user, request);
        return "Password recovery e-mail was sent successfully";
    }

    public String setNewPassword(ResetPasswordDto resetPasswordDto) {
        ConfirmationToken confirmationToken = authenticationService.validateAndReturnToken(resetPasswordDto.getToken());
        User user = confirmationToken.getUser();
        if (authenticationService.arePasswordsMatching(
                resetPasswordDto.getNewPassword(),
                resetPasswordDto.getRepeatPassword())) {
            user.setPassword(SecurityConfig.passwordEncoder().encode(resetPasswordDto.getNewPassword()));
            userRepository.save(user);
            confirmationTokenRepository.delete(confirmationToken); //delete the token after new pw is set
            return "Password changed successfully";
        }
        throw new PasswordsNotMatchingException("Passwords do not match");
    }
}
