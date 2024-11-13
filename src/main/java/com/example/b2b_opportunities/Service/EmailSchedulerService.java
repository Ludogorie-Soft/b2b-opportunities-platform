package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Filter;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Static.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailSchedulerService {
    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final MailService mailService;


//    @Scheduled(cron = "0 0 9 * * MON")
    @Scheduled(cron = "${cron.everyMondayAt9}")
    public void sendEmailEveryMonday() {
        List<Project> projectsLastThreeDays = getProjectsUpdatedInPastDays(3);
        sendEmailToEveryCompany(projectsLastThreeDays);
    }

//    @Scheduled(cron = "0 0 9 * * 2-5")
    @Scheduled(cron = "${cron.TuesdayToFridayAt9}")
    public void sendEmailTuesdayToFriday() {
        List<Project> projectsLastOneDay = getProjectsUpdatedInPastDays(1);
        sendEmailToEveryCompany(projectsLastOneDay);
    }

    /**
     * This method will only send emails to companies that don't have any skills set and Default filter is Enabled.
     * This will remind them to set their skills or to create filters
     */
//    @Scheduled(cron = "0 0 9 * * MON")
    @Scheduled(cron = "${cron.companiesNoSkillsAndNoCustomFilters}")
    public void sendWeeklyEmailsWhenCompanyHasNoSkillsAndNoCustomFilters() {
        List<Project> projectsLastWeek = getProjectsUpdatedInPastDays(7);
        String title = "B2B Important: Set Your Company Skills to Receive Relevant Project Updates";
        String emailContent = "Hello,\n\n" +
                "We noticed that you haven’t set any skills for your company profile yet. " +
                "This week alone, there were " + projectsLastWeek.size() + " new projects posted that may be relevant to you.\n\n" +
                "To ensure you receive notifications tailored to your interests, please update your company profile by adding relevant skills. " +
                "This way, you’ll only be notified about projects that align with your expertise.\n\n" +
                "Alternatively, you can create custom filters to further refine the types of projects you receive notifications about.\n\n" +
                "If you wish to stop receiving these updates altogether, simply disable the \"Default\" filter in your profile settings.\n\n" +
                "Thank you for staying with us, and we look forward to helping you find the right opportunities!\n\n" +
                "Best regards,\n B2B Opportunities";

        List<Company> companies = companyRepository.findCompaniesWithSingleDefaultEnabledFilterAndNoCompanySkills();
        for (Company c : companies) {
            mailService.sendEmail(c.getEmail(), emailContent, title);
        }
    }

//    @Scheduled(cron = "0 0 13 * * *") //Once per day at 13:00
    @Scheduled(cron = "${cron.processExpiringProjects}")
    public void processExpiringProjects() {
        List<Project> expiringProjects = projectRepository.findProjectsExpiringInTwoDays();
        for (Project project : expiringProjects) {
            mailService.sendProjectExpiringMail(project);
        }
        List<Project> expiredProjects = projectRepository.findExpiredAndActiveProjects();
        for (Project project : expiredProjects) {
            project.setProjectStatus(ProjectStatus.INACTIVE);
            projectRepository.save(project);
        }
    }

    private void sendEmailToEveryCompany(List<Project> projectsToCheck) {
        List<Company> companies = companyRepository.findAll();

        for (Company c : companies) {
            Set<Skill> skills = getAllEnabledCompanyFilterSkills(c);
            if (skills.isEmpty()) {
                // If the company has no Filters (or those don't have any skills),
                // and the 'Default' filter is Enabled - use the company skills (if any).
                // If the Default Filter has any skills added to it, it will be treated as 'Custom' filter
                // and the skills will be taken from getAllEnabledCompanyFilterSkills.
                Filter defaultFilter = c.getFilters().stream()
                        .filter(f -> f.getName().equalsIgnoreCase("Default") && f.getIsEnabled())
                        .findFirst()
                        .orElse(null);

                if (defaultFilter != null) {
                    skills = c.getSkills();
                }
            }
            if (skills.isEmpty()) {
                // No need to continue. There is another scheduler that handles this:
                // sendWeeklyEmailsWhenCompanyHasNoSkillsAndNoCustomFilters
                return;
            }

            Set<Project> projectsThatMatchAtLeastOneSkill = getMatchingProjects(skills, projectsToCheck);

            boolean hasChanged = false;
            Set<Project> newProjects = new HashSet<>();
            Set<Project> modifiedProjects = new HashSet<>();
            for (Project p : projectsThatMatchAtLeastOneSkill) {
                if (!c.getProjectIdsNotified().contains(p.getId())) {
                    newProjects.add(p);
                    c.getProjectIdsNotified().add(p.getId());
                    hasChanged = true;
                } else {
                    modifiedProjects.add(p);
                }
            }

            if (hasChanged) {
                companyRepository.save(c);
            }

            if (!newProjects.isEmpty() || !modifiedProjects.isEmpty()) {
                String emailContent = generateEmailContent(newProjects, modifiedProjects);
                String receiver = c.getEmail();
                String title = "B2B Don't miss on new projects";
                mailService.sendEmail(receiver, emailContent, title);
            }
        }
    }

    private String generateEmailContent(Set<Project> newProjects, Set<Project> modifiedProjects) {
        StringBuilder result = new StringBuilder();
        result.append("Hello,\n\n");

        if (!newProjects.isEmpty()) {
            result.append("There are new projects available for you that match some of your skills:\n");

            for (Project project : newProjects) {
                result.append("Project ID: ").append(project.getId()).append("\n");
                // TODO - return front end address to the projects
            }

            result.append("\n");
        }

        if (!modifiedProjects.isEmpty()) {
            if (!newProjects.isEmpty()) {
                result.append("You might also want to checkout some of the projects that got modified recently:\n");
            } else {
                result.append("There are some projects that got modified recently:\n");
            }

            for (Project project : modifiedProjects) {
                result.append("Modified Project ID: ").append(project.getId()).append("\n");
            }
        }

        result.append("\nThank you for your attention!");

        return result.toString();
    }

    private Set<Project> getMatchingProjects(Set<Skill> skills, List<Project> projectsToCheck) {
        Set<Project> matchingProjects = new HashSet<>();
        for (Skill skill : skills) {
            Project p = getProjectIfContainsSkill(skill, projectsToCheck);
            if (p != null) {
                matchingProjects.add(p);
            }
        }
        return matchingProjects;
    }

    private Set<Skill> getAllEnabledCompanyFilterSkills(Company company) {
        Set<Skill> skills = new HashSet<>();
        for (Filter filter : company.getFilters()) {
            if (filter.getIsEnabled()) {
                skills.addAll(filter.getSkills());
            }
        }
        return skills;
    }

    private Project getProjectIfContainsSkill(Skill skill, List<Project> projectsToCheck) {
        for (Project p : projectsToCheck) {
            for (Position position : p.getPositions()) {
                List<Skill> skills = position.getRequiredSkills().stream().map(RequiredSkill::getSkill).toList();
                if (skills.contains(skill) || position.getOptionalSkills().contains(skill)) {
                    return p;
                }
            }
        }
        return null;
    }

    private List<Project> getProjectsUpdatedInPastDays(int days) {
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);
        return projectRepository.findAllByDateUpdatedAfter(dateThreshold);
    }
}
