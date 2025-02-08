package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.EmailRequest;
import com.example.b2b_opportunities.Entity.*;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final RestTemplate restTemplate;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${password.recovery.url}")
    private String passwordRecoveryUrl;

    public String createAndSaveUserToken(User user) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, user);
        confirmationTokenRepository.save(confirmationToken);
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
        sendEmail(user.getEmail(), emailContent, subject);
        log.info("Send confirmation Email to: {}", user.getEmail());
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
        log.info("Send password recovery Email to: {}", user.getEmail());
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
        log.info("Send company (ID: {}) confirmation Email to: {}", company.getId(), company.getEmail());
    }

    public void sendProjectExpiringMail(Project project) {
        String emailContent = "<html>" +
                "<body>" +
                "<h2>Dear " + project.getCompany().getName() + ",</h2>"
                + "<h3><br/> This is a friendly reminder regarding your project '" + project.getName() + "' will expire in <b>2 days</b>."
                + "<br/>To ensure your project remains active and continues to be visible to potential clients, you can easily extend its duration."
                + "<br/>To extend your project for an additional 3 weeks, please visit our website.</h3>" +
                "<h3><br/> Best regards,\n" +
                "The B2B Opportunities Team,<br/></h3>" +
                "</body>" +
                "</html>";
        String subject = "B2B Reminder: Your Project is Expiring in 2 Days – Reactivate Now!";
        String email = project.getCompany().getEmail();
        sendEmail(email, emailContent, subject);
        log.info("Send project expiring soon Email to: {}", email);
    }

    public void sendEmailWhenApplicationIsApproved(PositionApplication positionApplication) {
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("<html>").append(System.lineSeparator())
                .append("<body>").append(System.lineSeparator())
                .append("<h2>Dear ").append(positionApplication.getTalentCompany().getName()).append(",</h2>")
                .append(System.lineSeparator())
                .append("<p>").append(System.lineSeparator())
                .append("Congratulations! We are pleased to inform you that your application for the position of '")
                .append("<strong>")
                .append(positionApplication.getPosition().getPattern().getName())
                .append("</strong>")
                .append("' has been approved.")
                .append(System.lineSeparator())
                .append("We are excited to move forward with the next steps and will be in touch with more details soon.")
                .append(System.lineSeparator())
                .append("If you have any questions or need further information, feel free to reach out to us.")
                .append("</p>").append(System.lineSeparator())
                .append("<strong>").append(System.lineSeparator())
                .append("Best regards,").append(System.lineSeparator())
                .append("The B2B Opportunities Team").append(System.lineSeparator())
                .append("</strong>").append(System.lineSeparator())
                .append("</body>").append(System.lineSeparator())
                .append("</html>").append(System.lineSeparator());

        String subject = "Your Application Has Been Approved - B2B Opportunities";
        String email = positionApplication.getTalentCompany().getEmail();

        sendEmail(email, emailContent.toString(), subject);
        log.info("Send application approval Email to: {}", email);
    }

    public void sendEmail(String receiver, String content, String subject) {
        EmailRequest er = EmailRequest.builder()
                .receiver(receiver)
                .content(content)
                .subject(subject)
                .build();

        restTemplate.postForObject(emailServiceUrl, er, String.class);
    }

    private String generateConfirmationLink(User user, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String token = createAndSaveUserToken(user);
        return "<a href=" + baseUrl + "/api/auth/register/confirm?token=" + token + ">Confirm Email</a>";
    }

    private String generatePasswordRecoveryLink(User user, HttpServletRequest request) {
//        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String token = createAndSaveUserToken(user);
        return "<a href=" + passwordRecoveryUrl + token + ">Reset password</a>";
    }

    private String generateEmailConfirmationLink(String token, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return "<a href=" + baseUrl + "/companies/confirm-email?token=" + token + ">Confirm your email address</a>";
    }
}