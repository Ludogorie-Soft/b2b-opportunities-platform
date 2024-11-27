package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Config.SecurityConfig;
import com.example.b2b_opportunities.Dto.Request.ResetPasswordDto;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.PasswordsNotMatchingException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {
    private final MailService mailService;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserService userService;

    public String requestPasswordRecovery(String email, HttpServletRequest request) {
        User user = userService.getUserByEmailOrThrow(email);
        log.info("Attempting password recovery for user ID: {}", user.getId());
        if (user.getProvider() != null)
            throw new PermissionDeniedException("User registered using oAuth - " + user.getProvider());
        if (!user.isEnabled()) {
            throw new AuthenticationFailedException("Account not activated");
        }
        mailService.sendPasswordRecoveryMail(user, request);
        log.info("Sending password recovery mail for user ID: {}", user.getId());
        return "Password recovery e-mail was sent successfully";
    }

    public String setNewPassword(ResetPasswordDto resetPasswordDto) {
        ConfirmationToken confirmationToken = authenticationService.validateAndReturnToken(resetPasswordDto.getToken());
        User user = confirmationToken.getUser();
        log.info("Attempting password change user ID: {}", user.getId());
        if (authenticationService.arePasswordsMatching(
                resetPasswordDto.getNewPassword(),
                resetPasswordDto.getRepeatPassword())) {
            user.setPassword(SecurityConfig.passwordEncoder().encode(resetPasswordDto.getNewPassword()));
            userRepository.save(user);
            confirmationTokenRepository.delete(confirmationToken); //delete the token after new pw is set
            log.info("Changing password for user ID: {}", user.getId());
            return "Password changed successfully";
        }
        throw new PasswordsNotMatchingException("Passwords do not match", "password");
    }
}
