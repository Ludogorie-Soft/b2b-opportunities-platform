package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.ServerErrorException;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromMail;

    private String generateConfirmationLink(User user, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String token = confirmationTokenService.generateConfirmationCode(user);
        return "<a href=" + baseUrl + "/api/auth/register/confirm?token=" + token + ">Confirm Email</a>";
    }

    private String generatePasswordRecoveryLink(User user, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String token = generatePasswordRecoveryToken(user);
        return "<a href=" + baseUrl + "/api/auth/reset-password?token=" + token + ">Reset password</a>";
    }

    private String generatePasswordRecoveryToken(User user) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken recoveryToken = new ConfirmationToken(
                token,
                user
        );
        confirmationTokenRepository.save(recoveryToken);
        return token;
    }

    public void sendConfirmationMail(User user, HttpServletRequest request) {
        String emailContent = "<html>" +
                "<body>" +
                "<h2>Dear " + user.getFirstName() + ",</h2>"
                + "<br/> We're excited to have you get started. " +
                "Please click on below link to confirm your account."
                + "<br/> " + generateConfirmationLink(user, request) +
                "<br/> Regards,<br/>" +
                "B2B Opportunities Team" +
                "</body>" +
                "</html>";

        String subject = "Confirm your E-Mail - B2B Opportunities";
        sendEmail(user, emailContent, subject);
    }

    private void sendEmail(User user, String content, String subject) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(user.getEmail());
            helper.setFrom(fromMail);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new ServerErrorException("An error occurred while sending the E-Mail. Please try again later.");
        }
    }

    public void sendPasswordRecoveryMail(User user, HttpServletRequest request) {
        String emailContent = "<html>" +
                "<body>" +
                "<h2>Dear " + user.getFirstName() + ",</h2>"
                + "<br/> Click the link below to set your new password: "
                + "<br/> " + generatePasswordRecoveryLink(user, request) +
                "<br/> Regards,<br/>" +
                "B2B Opportunities Team" +
                "</body>" +
                "</html>";

        String subject = "Reset password - B2B Opportunities";
        sendEmail(user, emailContent, subject);
    }
}