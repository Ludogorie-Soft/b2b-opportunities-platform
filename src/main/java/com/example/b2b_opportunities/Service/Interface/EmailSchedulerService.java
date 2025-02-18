package com.example.b2b_opportunities.Service.Interface;

public interface EmailSchedulerService {

    public void sendEmailEveryMonday();

    public void sendEmailTuesdayToFriday();

    public void sendWeeklyEmailsWhenCompanyHasNoSkillsAndNoCustomFilters();


    public void processExpiringProjects();


    public void processNewApplications();

}