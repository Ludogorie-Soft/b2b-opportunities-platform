package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.ServerErrorException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final ConfirmationTokenService confirmationTokenService;
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromMail;

    private String generateConfirmationLink(User user) {
        String token = confirmationTokenService.generateConfirmationCode(user);
        return "<a href=http://localhost:8082/api/auth/register/confirm?token=" + token + ">Confirm Email</a>";
    }

    public void sendConfirmationMail(User user) {
        String emailContent = "<html>" +
                "<body>" +
                "<h2>Dear " + user.getFirstName() + ",</h2>"
                + "<br/> We're excited to have you get started. " +
                "Please click on below link to confirm your account."
                + "<br/> " + generateConfirmationLink(user) +
                "<br/> Regards,<br/>" +
                "B2B Opportunities Team" +
                "</body>" +
                "</html>";

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(user.getEmail());
            helper.setFrom(fromMail);
            helper.setSubject("Confirm your E-Mail - B2B Opportunities");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new ServerErrorException("An error occurred while sending the E-Mail. Please try again later.");
        }
    }
}