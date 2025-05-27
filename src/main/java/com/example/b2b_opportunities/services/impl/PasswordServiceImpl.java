package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.config.SecurityConfig;
import com.example.b2b_opportunities.dto.requestDtos.ResetPasswordDto;
import com.example.b2b_opportunities.entity.ConfirmationToken;
import com.example.b2b_opportunities.entity.User;
import com.example.b2b_opportunities.exception.AuthenticationFailedException;
import com.example.b2b_opportunities.exception.PasswordsNotMatchingException;
import com.example.b2b_opportunities.exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.repository.UserRepository;
import com.example.b2b_opportunities.services.interfaces.MailService;
import com.example.b2b_opportunities.services.interfaces.PasswordService;
import com.example.b2b_opportunities.services.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {
    private final MailService mailService;
    private final UserRepository userRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserService userService;

    @Override
    public String requestPasswordRecovery(String email, HttpServletRequest request) {
        User user = userService.getUserByEmailOrThrow(email);
        log.info("Attempting password recovery for user ID: {}", user.getId());
        if (user.getProvider() != null)
            throw new PermissionDeniedException("User registered using oAuth - " + user.getProvider());
        if (!user.isEnabled()) {
            throw new AuthenticationFailedException("Account not activated");
        }
        deleteTokenIfExists(user);
        mailService.sendPasswordRecoveryMail(user, request);
        log.info("Sending password recovery mail for user ID: {}", user.getId());
        return "Password recovery e-mail was sent successfully";
    }

    @Override
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

    private void deleteTokenIfExists(User user){
        Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findByUser(user);
        confirmationToken.ifPresent(confirmationTokenRepository::delete);
    }
}
