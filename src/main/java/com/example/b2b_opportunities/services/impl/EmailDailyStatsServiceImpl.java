package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.entity.EmailDailyStats;
import com.example.b2b_opportunities.repository.EmailDailyStatsRepository;
import com.example.b2b_opportunities.services.interfaces.EmailDailyStatsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class EmailDailyStatsServiceImpl implements EmailDailyStatsService {

    private final EmailDailyStatsRepository emailDailyStatsRepository;

    @Transactional
    private void incrementField(Consumer<EmailDailyStats> incrementFunction){
        EmailDailyStats stats = findOrCreateStatsByDay();
        incrementFunction.accept(stats);
        emailDailyStatsRepository.save(stats);
    }

    public void incrementActivationMailsSent() {
        incrementField(stats -> stats.setActivationMailsSent(stats.getActivationMailsSent() + 1));
    }

    public void incrementActivationMailsOpened() {
        incrementField(stats -> stats.setActivationMailsOpened(stats.getActivationMailsOpened() + 1));
    }

    public void incrementNewProjectsSent() {
        incrementField(stats -> stats.setNewProjectMailsSent(stats.getNewProjectMailsSent() + 1));
    }

    public void incrementNewApplicationMailsSent() {
        incrementField(stats -> stats.setNewApplicationMailsSent(stats.getNewApplicationMailsSent()+1));
    }

    private EmailDailyStats findOrCreateStatsByDay() {
        LocalDate day = LocalDate.now();
        return emailDailyStatsRepository.findByDay(day)
                .orElseGet(() -> {
                    EmailDailyStats newStats = new EmailDailyStats();
                    newStats.setDay(day);
                    return emailDailyStatsRepository.save(newStats);
                });
    }
}
