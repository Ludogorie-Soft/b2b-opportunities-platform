package com.example.b2b_opportunities.Service.Implementation;

import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Filter;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Service.Interface.EmailSchedulerService;
import com.example.b2b_opportunities.Service.Interface.MailService;
import com.example.b2b_opportunities.Service.Interface.PositionApplicationService;
import com.example.b2b_opportunities.Static.ProjectStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Value("${frontend.address}")
    private String frontEndAddress;

    @Transactional
    @Scheduled(cron = "${cron.everyMondayAt9}")
    @Override
    public void sendEmailEveryMonday() {
        List<Project> projectsLastThreeDays = getProjectsUpdatedInPastDays(3);
        sendEmailToEveryCompany(projectsLastThreeDays, 3);
    }

    @Transactional
    @Scheduled(cron = "${cron.TuesdayToFridayAt9}")
    @Override
    public void sendEmailTuesdayToFriday() {
        List<Project> projectsLastOneDay = getProjectsUpdatedInPastDays(1);
        sendEmailToEveryCompany(projectsLastOneDay, 1);
    }

    /**
     * This method will only send emails to companies that don't have any skills set and Default filter is Enabled.
     * This will remind them to set their skills or to create filters
     */
    @Scheduled(cron = "${cron.companiesNoSkillsAndNoCustomFilters}")
    @Override
    public void sendWeeklyEmailsWhenCompanyHasNoSkillsAndNoCustomFilters() {
        List<Company> companies = companyRepository.findCompaniesWithSingleDefaultEnabledFilterAndNoCompanySkills();
        List<Project> projectsLastWeek = getProjectsUpdatedInPastDays(7);
        if(projectsLastWeek.isEmpty()) return;
        for (Company c : companies) {
            String title = "B2B Important: Set Your Company Skills to Receive Relevant Project Updates";
            String emailContent = "<html><body style=\"font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; font-weight: normal;\">" + "<p><b>Hello, " + c.getName() + ",</b></p>" +
                    "<p><b>We noticed your company profile has no skills listed yet.</b></p>" +
                    "This week, <b>" + projectsLastWeek.size() + " new projects </b> were posted that might interest you.<br/><br/>" +
                    "To receive more relevant notifications:<br/> " +
                    "<ul> <li> Add skills to your profile </li> " +
                    "<li>Create custom filters</li> " +
                    "<li>Or disable the <strong>\"Default\"</strong> filter to stop these updates</li> </ul>" +
                    "<br/>Thank you for staying with us, and we look forward to helping you find the right opportunities!<br/><br/>" +
                    "<br/><strong>Best regards,</strong>" +
                    "<br/><strong>hire-b2b team</strong>" +
                    "</body>" +
                    "</html>";

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
                .append("<br/><strong>Best regards,</strong>" +
                        "<br/><strong>hire-b2b team</strong>" +
                        "</body></html>");
        return emailContent.toString();
    }

    private void sendEmailToEveryCompany(List<Project> projectsToCheck, int daysPassed) {
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
                skills = c.getSkills();
            }

            Set<Project> matchingProjects = getMatchingProjects(skills, projectsToCheck);
            Set<Project> availableNewProjectsThatMatchAtLeastOneSkill = removeCurrentCompanyProjects(matchingProjects, c);

            processNewAndModifiedProjects(availableNewProjectsThatMatchAtLeastOneSkill, c, daysPassed);
        }
    }

    private Filter getDefaultFIlter(Company company) {
        return company.getFilters().stream()
                .filter(f -> f.getName().equalsIgnoreCase("Default") && f.getIsEnabled())
                .findFirst()
                .orElse(null);
    }

    private void processNewAndModifiedProjects(Set<Project> projects, Company c, int daysPassed) {
        Set<Project> newProjects = getModifiedOrNewProjects(daysPassed, "new", projects);
        Set<Project> modifiedProjects = getModifiedOrNewProjects(daysPassed, "modified", projects);

        Set<Long> newProjectIds = projects.stream()
                .map(Project::getId)
                .filter(id -> !c.getProjectIdsNotified().contains(id))
                .collect(Collectors.toSet());

        if (!newProjectIds.isEmpty()) {
            c.getProjectIdsNotified().addAll(newProjectIds);
            companyRepository.save(c);
        }

        modifiedProjects.removeAll(newProjects);

        sendEmailIfAnyNewOrModifiedProjects(newProjects, modifiedProjects, c);
    }

    private Set<Project> getModifiedOrNewProjects(int daysPassed, String status, Set<Project> projects) {
        LocalDate startDate = LocalDate.now().minusDays(daysPassed);
        LocalDate endDate = LocalDate.now();

        return projects.stream()
                .filter(p -> "new".equalsIgnoreCase(status)
                        ? isWithinRange(p.getDatePosted().toLocalDate(), startDate, endDate)
                        : isWithinRange(p.getDateUpdated().toLocalDate(), startDate, endDate))
                .collect(Collectors.toSet());
    }

    private boolean isWithinRange(LocalDate date, LocalDate start, LocalDate end) {
        return (date.isEqual(start) || date.isAfter(start)) && (date.isBefore(end) || date.isEqual(end));
    }

    private void sendEmailIfAnyNewOrModifiedProjects
            (Set<Project> newProjects, Set<Project> modifiedProjects, Company c) {
        if (!newProjects.isEmpty() || !modifiedProjects.isEmpty()) {
            String emailContent = generateEmailContent(c.getName(), newProjects, modifiedProjects);
            String receiver = c.getEmail();
            String title = "Hire-B2B - Don't miss on new projects";
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


    private String generateEmailContent(String
                                                companyName, Set<Project> newProjects, Set<Project> modifiedProjects) {
        StringBuilder result = new StringBuilder();
        result.append("<html><body style=\"font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; font-weight: normal;\">");
        result.append("<p><b>Hello, ").append(companyName).append(", </b></p>");

        if (!newProjects.isEmpty()) {
            result.append("<p> There are new projects available for you that match some of your skills: <br/>");
            result.append(getProjectLinks(newProjects));
        }

        if (!modifiedProjects.isEmpty()) {
            if (!newProjects.isEmpty()) {
                result.append("<p>You might also want to checkout some of the projects that got modified recently:<br/></p>");
            } else {
                result.append("<p>There are some projects that got modified recently:<br/></p>");
            }
            result.append(getProjectLinks(modifiedProjects));
        }

        result.append("<p>Thank you for your attention!</p>")
                .append("<br/><strong>Best regards,</strong>" +
                        "<br/><strong>hire-b2b team</strong>" +
                        "</body></html>");

        return result.toString();
    }

    private String getProjectLinks(Set<Project> projects) {
        StringBuilder result = new StringBuilder();
        result.append("<ul>");
        for (Project project : projects) {
            Long projectId = project.getId();

            result.append("<li><a href=\"").append(frontEndAddress).append("/")
                    .append("project/")
                    .append(projectId)
                    .append("\">")
                    .append(project.getName())
                    .append("</a>")
                    .append("<br/></li>");
        }
        result.append("</ul><br/>");
        return result.toString();
    }

    private Set<Project> getMatchingProjects(Set<Skill> skills, List<Project> projectsToCheck) {
        return projectsToCheck.stream()
                .filter(project -> project.getPositions().stream()
                        .anyMatch(position -> position.getRequiredSkills().stream()
                                .anyMatch(reqSkill -> skills.contains(reqSkill.getSkill()))
                                || position.getOptionalSkills().stream()
                                .anyMatch(skills::contains)))
                .collect(Collectors.toSet());
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

    private List<Project> getProjectsUpdatedInPastDays(int days) {
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);
        return projectRepository.findAllByDateUpdatedAfter(dateThreshold);
    }
}