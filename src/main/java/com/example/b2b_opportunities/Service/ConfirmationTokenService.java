package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final CompanyRepository companyRepository;

    public String generateUserToken(User user) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                user
        );
        confirmationTokenRepository.save(confirmationToken);
        return token;
    }

    public String generateCompanyToken(Company company) {
        String token = UUID.randomUUID().toString();
        company.setEmailConfirmationToken(token);
        companyRepository.save(company);
        return token;
    }
}