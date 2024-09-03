package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.Employer;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final ConfirmationTokenService confirmationTokenService;

    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromMail;

    public void sendConfirmationMail(Employer employer) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromMail);
        simpleMailMessage.setSubject("B2b opportunities - mail confirmation");
        simpleMailMessage.setText("Welcome, " + employer.getUsername() + "!\nUse this link to activate your profile: " + "http://localhost:8082/api/auth/register/confirm?token=" + confirmationTokenService.generateConfirmationCode(employer) + "\n\nBR, B2B Opportunities team.");
        simpleMailMessage.setTo(employer.getEmail());

        mailSender.send(simpleMailMessage);
    }

}
