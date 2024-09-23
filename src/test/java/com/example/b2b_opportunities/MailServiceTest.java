package com.example.b2b_opportunities;

import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Service.ConfirmationTokenService;
import com.example.b2b_opportunities.Service.MailService;
import jakarta.mail.MessagingException;
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ConfirmationTokenService confirmationTokenService;

    @InjectMocks
    private MailService mailService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "fromMail", "sender@test.com");
    }

    @Test
    void testSendConfirmationMail() throws MessagingException {
        User user = new User();
        user.setFirstName("Test");
        user.setEmail("test@test.com");

        when(confirmationTokenService.generateConfirmationCode(user)).thenReturn("generatedToken");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.sendConfirmationMail(user, request);

        verify(mailSender, times(1)).send(mimeMessage);
        verify(mailSender, times(1)).createMimeMessage();
        verify(mimeMessage, times(1)).setSubject("Confirm your E-Mail - B2B Opportunities");
    }
}

