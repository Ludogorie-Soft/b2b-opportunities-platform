package com.example.b2b_opportunities.Service.Implementation;

import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Filter;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Service.Interface.EmailSchedulerService;
import com.example.b2b_opportunities.Service.Interface.MailService;
import com.example.b2b_opportunities.Service.Interface.PositionApplicationService;
import com.example.b2b_opportunities.Static.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailSchedulerServiceImpl implements EmailSchedulerService {
    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final MailService mailService;
    private final PositionApplicationService positionApplicationService;

    @Scheduled(cron = "${cron.everyMondayAt9}")
    @Override
    public void sendEmailEveryMonday() {
        List<Project> projectsLastThreeDays = getProjectsUpdatedInPastDays(3);
        sendEmailToEveryCompany(projectsLastThreeDays);
    }

    @Scheduled(cron = "${cron.TuesdayToFridayAt9}")
    @Override
    public void sendEmailTuesdayToFriday() {
        List<Project> projectsLastOneDay = getProjectsUpdatedInPastDays(1);
        sendEmailToEveryCompany(projectsLastOneDay);
    }

    /**
     * This method will only send emails to companies that don't have any skills set and Default filter is Enabled.
     * This will remind them to set their skills or to create filters
     */
    @Scheduled(cron = "${cron.companiesNoSkillsAndNoCustomFilters}")
    @Override
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

    @Scheduled(cron = "${cron.processExpiringProjects}")
    @Override
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

    @Scheduled(cron = "0 0 10 * * MON-FRI")
    @Override
    public void processNewApplications() {
        List<PositionApplication> positionApplications = positionApplicationService.getApplicationsSinceLastWorkday();
        if (positionApplications.isEmpty()) {
            return;
        }
        Map<Company, Map<Position, Long>> companyPositionApplicationCount = positionApplications.stream()
                .collect(Collectors.groupingBy(
                        pa -> pa.getPosition().getProject().getCompany(),
                        Collectors.groupingBy(PositionApplication::getPosition, Collectors.counting())
                ));

        companyPositionApplicationCount.forEach((company, positionCountMap) -> {
            String emailContent = buildNewApplicationsEmailContent(positionCountMap);
            mailService.sendEmail(company.getEmail(), emailContent, "New Job Applications for Your Positions");
        });
    }

    private String buildNewApplicationsEmailContent(Map<Position, Long> positionCountMap) {
        String companyName = positionCountMap.keySet().iterator().next().getProject().getCompany().getName();

        StringBuilder emailContent = new StringBuilder("<html><body style=\"font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; font-weight: normal;\">")
                .append("<p><b>Dear ").append(companyName).append(",</b></p>")
                .append("<p><b>Great news! You have received new job applications for your open positions:</b></p>");

        positionCountMap.forEach((position, count) ->
                emailContent.append("<p><b>")
                        .append(position.getPattern().getName())
                        .append("</b>")
                        .append(" - ").append(count)
                        .append(" application(s)")
                        .append("</p>")
        );

        emailContent.append("<p><b>You can review the applications on your dashboard.</b></p>")
                .append("<p><b>Best regards,<br/>B2B Opportunities Team</b></p></body></html>");
        return emailContent.toString();
    }

    private void sendEmailToEveryCompany(List<Project> projectsToCheck) {
        List<Company> companies = companyRepository.findAll();

        for (Company c : companies) {
            Set<Skill> skills = getAllEnabledCompanyFilterSkills(c);
            if (skills.isEmpty()) {
                Filter defaultFilter = getDefaultFIlter(c);
                if (defaultFilter != null) {
                    skills = c.getSkills();
                }
            }
            if (skills.isEmpty()) {
                return;
            }

            Set<Project> matchingProjects = getMatchingProjects(skills, projectsToCheck);
            Set<Project> availableNewProjectsThatMatchAtLeastOneSkill = removeCurrentCompanyProjects(matchingProjects, c);

            processNewAndModifiedProjects(availableNewProjectsThatMatchAtLeastOneSkill, c);
        }
    }

    private Filter getDefaultFIlter(Company company) {
        return company.getFilters().stream()
                .filter(f -> f.getName().equalsIgnoreCase("Default") && f.getIsEnabled())
                .findFirst()
                .orElse(null);
    }

    private void processNewAndModifiedProjects(Set<Project> projects, Company c) {
        boolean hasChanged = false;
        Set<Project> newProjects = new HashSet<>();
        Set<Project> modifiedProjects = new HashSet<>();

        for (Project p : projects) {
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

        sendEmailIfAnyNewOrModifiedProjects(newProjects, modifiedProjects, c);
    }

    private void sendEmailIfAnyNewOrModifiedProjects(Set<Project> newProjects, Set<Project> modifiedProjects, Company c) {
        if (!newProjects.isEmpty() || !modifiedProjects.isEmpty()) {
            String emailContent = generateEmailContent(newProjects, modifiedProjects);
            String receiver = c.getEmail();
            String title = "B2B Don't miss on new projects";
            mailService.sendEmail(receiver, emailContent, title);
        }
    }

    private Set<Project> removeCurrentCompanyProjects(Set<Project> projects, Company company) {
        return projects.stream()
                .filter(project -> isAccessibleByCompany(project, company))
                .collect(Collectors.toSet());
    }

    private boolean isAccessibleByCompany(Project project, Company company) {
        if (!Objects.equals(project.getCompany().getId(), company.getId())) {
            if (!project.isPartnerOnly()) {
                return true;
            }
            return project.getPartnerGroupList().stream()
                    .flatMap(group -> group.getPartners().stream())
                    .anyMatch(partner -> Objects.equals(partner.getId(), company.getId()));
        }
        return false;
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