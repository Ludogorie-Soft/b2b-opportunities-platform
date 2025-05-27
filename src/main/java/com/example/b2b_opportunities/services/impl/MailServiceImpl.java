package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.dto.requestDtos.EmailRequest;
import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.ConfirmationToken;
import com.example.b2b_opportunities.entity.PositionApplication;
import com.example.b2b_opportunities.entity.Project;
import com.example.b2b_opportunities.entity.User;
import com.example.b2b_opportunities.repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.services.interfaces.MailService;
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
public class MailServiceImpl implements MailService {
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final RestTemplate restTemplate;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${password.recovery.url}")
    private String passwordRecoveryUrl;

    @Value("${frontend.address}")
    private String frontEndAddress;

    @Override
    public String createAndSaveUserToken(User user) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, user);
        confirmationTokenRepository.save(confirmationToken);
        return token;
    }

    @Override
    public void sendConfirmationMail(User user, HttpServletRequest request) {
        String emailContent = "<html>" +
                "<body>" +
                "<h2>Dear " + user.getFirstName() + ",</h2>"
                + "<br/> We're excited to have you get started. " +
                "Please click on the link below to confirm your account."
                + "<br/> " + generateConfirmationLink(user, request) +
                "<br/><strong>Best regards,</strong>" +
                "<br/><strong>hire-b2b team</strong>" +
                "</body>" +
                "</html>";

        String subject = "Confirm your E-Mail at hire-b2b.com";
        sendEmail(user.getEmail(), emailContent, subject);
        log.info("Send confirmation Email to: {}", user.getEmail());
    }

    @Override
    public void sendPasswordRecoveryMail(User user, HttpServletRequest request) {
        String emailContent = "<html>" +
                "<body>" +
                "<h2>Dear " + user.getFirstName() + ",</h2>"
                + "<br/> Click the link below to set your new password: "
                + "<br/> " + generatePasswordRecoveryLink(user, request) +
                "<br/><strong>Best regards,</strong>" +
                "<br/><strong>hire-b2b team</strong>" +
                "</body>" +
                "</html>";

        String subject = "Reset password at hire-b2b.com";
        sendEmail(user.getEmail(), emailContent, subject);
        log.info("Send password recovery Email to: {}", user.getEmail());
    }

    @Override
    public void sendCompanyEmailConfirmation(Company company, String token, HttpServletRequest request) {
        String emailContent = "<html>" +
                "<body>" +
                "<h2>Hello " + company.getName() + ",</h2>"
                + "<h3><br/> Thank you for registering your company with hire-b2b."
                + "<br/>To complete your registration and confirm your email address, please click the link below: </h3>"
                + "<h2> <br/> " + generateEmailConfirmationLink(token, request) + "</h2>" +
                "<br/><strong>Best regards,</strong>" +
                "<br/><strong>hire-b2b team</strong>" +
                "</body>" +
                "</html>";
        String subject = "Company mail confirmation - hire-b2b Team";
        sendEmail(company.getEmail(), emailContent, subject);
        log.info("Send company (ID: {}) confirmation Email to: {}", company.getId(), company.getEmail());
    }

    @Override
    public void sendProjectExpiringMail(Project project) {
        String emailContent = "<html><body style=\"font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; font-weight: normal;\">"
                + "<p><b>Dear " + project.getCompany().getName() + ",</b></p>"
                + "<p><br/> This is a friendly reminder that your project '<i><b>" + project.getName() + "</b></i>' will expire in <b>2 days</b>."
                + "<br/>To ensure your project remains active and continues to be visible to potential clients, you can easily extend its duration from " +
                "<a href=\"" + frontEndAddress + "/project/" + project.getId() + "/edit\">here.</a>"+
                "<br/><strong>Best regards,</strong>" +
                "<br/><strong>hire-b2b team</strong>" +
                "</body>" +
                "</html>";
        String subject = "B2B Reminder: Your Project is Expiring in 2 Days – Reactivate Now!";
        String email = project.getCompany().getEmail();
        sendEmail(email, emailContent, subject);
        log.info("Send project expiring soon Email to: {}", email);
    }

    @Override
    public void sendEmailWhenApplicationIsApproved(PositionApplication positionApplication) {

        String emailContent = "<html>" +
                "<body>" +
                "<h2>Dear " + positionApplication.getTalentCompany().getName() + ",</h2>" +
                "<br/>Congratulations! We are pleased to inform you that your application for the position of " +
                "<strong>'" + positionApplication.getPosition().getPattern().getName() + "'</strong> has been approved." +
                "<br/>In your account you can now view details and contacts of the company that published the position." +
                "<a href=\"" + frontEndAddress + "/company/applications\">Click here</a> to open it." +
                "<br/><strong>Best regards,</strong>" +
                "<br/><strong>hire-b2b team</strong>" +
                "</body>" +
                "</html>";

        String subject = "Your Application Has Been Approved at hire-b2b.com";
        String email = positionApplication.getTalentCompany().getEmail();

        sendEmail(email, emailContent, subject);
        log.info("Send application approval Email to: {}", email);
    }

    @Override
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