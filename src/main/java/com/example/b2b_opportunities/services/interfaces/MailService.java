package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.PositionApplication;
import com.example.b2b_opportunities.entity.Project;
import com.example.b2b_opportunities.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface MailService {
    String createAndSaveUserToken(User user);

    void sendConfirmationMail(User user, HttpServletRequest request);

    void sendPasswordRecoveryMail(User user, HttpServletRequest request);

    void sendCompanyEmailConfirmation(Company company, String token, HttpServletRequest request);

    void sendProjectExpiringMail(Project project);

    void sendEmailWhenApplicationIsApproved(PositionApplication positionApplication);

    void sendEmail(String receiver, String content, String subject);
}
