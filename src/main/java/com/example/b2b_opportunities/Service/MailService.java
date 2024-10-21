package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.EmailRequest;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailService {
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final RestTemplate restTemplate;

    @Value("${email.service.url}")
    private String emailServiceUrl;

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
        sendEmail(project.getCompany().getEmail(), emailContent, subject);
    }
  
    private void sendEmail(String receiver, String content, String subject) {
        EmailRequest er = EmailRequest.builder()
                .receiver(receiver)
                .content(content)
                .subject(subject)
                .build();

        String response = restTemplate.postForObject(emailServiceUrl, er, String.class);
        System.out.println(response);
    }

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

    private String generateEmailConfirmationLink(String token, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return "<a href=" + baseUrl + "/companies/confirm-email?token=" + token + ">Confirm your email address</a>";
    }
}