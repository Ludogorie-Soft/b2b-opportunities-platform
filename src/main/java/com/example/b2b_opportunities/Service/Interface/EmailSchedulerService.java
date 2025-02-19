package com.example.b2b_opportunities.Service.Interface;

import org.springframework.scheduling.annotation.Scheduled;

public interface EmailSchedulerService {


    @Scheduled(cron = "${cron.everyMondayAt9}")
    void sendEmailEveryMonday();

    @Scheduled(cron = "${cron.TuesdayToFridayAt9}")
    void sendEmailTuesdayToFriday();

    @Scheduled(cron = "${cron.companiesNoSkillsAndNoCustomFilters}")
    void sendWeeklyEmailsWhenCompanyHasNoSkillsAndNoCustomFilters();

    @Scheduled(cron = "${cron.processExpiringProjects}")
    void processExpiringProjects();

    @Scheduled(cron = "0 0 10 * * MON-FRI")
    void processNewApplications();
}
