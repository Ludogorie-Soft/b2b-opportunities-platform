package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.dto.requestDtos.EmailRequest;
import com.example.b2b_opportunities.entity.User;
import com.example.b2b_opportunities.repository.ConfirmationTokenRepository;
import com.example.b2b_opportunities.services.impl.MailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private MailServiceImpl mailService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "emailServiceUrl", "http://mocked-email-service.com/send");

    }

    @Test
    void testSendConfirmationMail() {
        User user = new User();
        user.setFirstName("Test");
        user.setEmail("test@test.com");

        when(restTemplate.postForObject(any(String.class), any(EmailRequest.class), eq(String.class)))
                .thenReturn("Email sent successfully");

        mailService.sendConfirmationMail(user, request);

        verify(restTemplate, times(1)).postForObject(any(String.class), any(EmailRequest.class), eq(String.class));
    }
}