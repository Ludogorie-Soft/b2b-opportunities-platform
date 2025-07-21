package com.example.b2b_opportunities.services.interfaces;

public interface EmailDailyStatsService {

    void incrementActivationMailsSent();
    void incrementActivationMailsOpened();
    void incrementNewProjectsSent();
    void incrementNewApplicationMailsSent();
}
