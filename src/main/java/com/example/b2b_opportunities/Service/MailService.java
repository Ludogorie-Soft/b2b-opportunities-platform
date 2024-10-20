package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.Project;
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
    private final JavaMailSender mailSender;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Value("${spring.mail.username}")
    private String fromMail;

    private String generateConfirmationLink(User user, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String token = createAndSaveUserToken(user);
        return "<a href=" + baseUrl + "/api/auth/register/confirm?token=" + token + ">Confirm Email</a>";
    }

    private String generatePasswordRecoveryLink(User user, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String token = createAndSaveUserToken(user);
        return "<a href=" + baseUrl + "/api/auth/reset-password?token=" + token + ">Reset password</a>";
    }

    public String createAndSaveUserToken(User user) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, user);
        confirmationTokenRepository.save(confirmationToken);
        return token;
    }

    private String generateEmailConfirmationLink(String token, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return "<a href=" + baseUrl + "/company/confirm-email?token=" + token + ">Confirm your email address</a>";
    }

    private String generateProjectExtendingLink(Project project, String backendAddress, String token) {
        return "<a href=" + backendAddress + "/projects/" + project.getId() + "/extend?token=" + token + ">Click here to extend your project</a>";
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
        sendEmail(user.getEmail(), emailContent, subject);
    }

    private void sendEmail(String receiver, String content, String subject) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(receiver);
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
        sendEmail(user.getEmail(), emailContent, subject);
    }

    public void sendCompanyEmailConfirmation(Company company, String token, HttpServletRequest request) {
        String emailContent = "<html>" +
                "<body>" +
                "<h2>Hello " + company.getName() + ",</h2>"
                + "<h3><br/> Thank you for registering your company with B2B Opportunities."
                + "<br/>To complete your registration and confirm your email address, please click the link below: </h3>"
                + "<h2> <br/> " + generateEmailConfirmationLink(token, request) + "</h2>" +
                "<h3><br/> Best regards,\n" +
                "The B2B Opportunities Team,<br/></h3>" +
                "</body>" +
                "</html>";
        String subject = "Company mail confirmation - B2B Opportunities";
        sendEmail(company.getEmail(), emailContent, subject);
    }

    public void sendProjectExpiringMail(Project project, String backendAddress, String token) {
        String emailContent = "<html>" +
                "<body>" +
                "<h2>Dear " + project.getCompany().getName() + ",</h2>"
                + "<h3><br/> This is a friendly reminder regarding your project '" + project.getName() + "' will expire in <b>2 days</b>."
                + "<br/>To ensure your project remains active and continues to be visible to potential clients, you can easily extend its duration."
                + "<br/>To extend your project for an additional 3 weeks, simply click the link below:.</h3>"
                + "<h2> <br/> " + generateProjectExtendingLink(project, backendAddress, token) + "</h2>" +
                "<h3><br/> Best regards,\n" +
                "The B2B Opportunities Team,<br/></h3>" +
                "</body>" +
                "</html>";
        String subject = "B2B Reminder: Your Project is Expiring in 2 Days – Reactivate Now!";
        sendEmail(project.getCompany().getEmail(), emailContent, subject);
    }
}