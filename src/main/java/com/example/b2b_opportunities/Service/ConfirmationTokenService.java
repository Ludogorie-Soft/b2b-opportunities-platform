package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.ConfirmationToken;
import com.example.b2b_opportunities.Entity.Employer;
import com.example.b2b_opportunities.Repository.ConfirmationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public String generateConfirmationCode(Employer employer) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                employer
        );
        confirmationTokenRepository.save(confirmationToken);
        return token;
    }
}
