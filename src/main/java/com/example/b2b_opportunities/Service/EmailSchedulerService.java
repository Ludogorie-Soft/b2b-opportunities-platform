package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Filter;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
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

    @Scheduled(cron = "0 0 9 * * MON")
    public void sendEmailEveryMonday() {
        List<Project> projectsLastThreeDays = getProjectsUpdatedInPastDays(3);
        sendEmailToEveryCompany(projectsLastThreeDays);
    }

    @Scheduled(cron = "0 0 9 * * 2-5")
    public void sendEmailTuesdayToFriday() {
        List<Project> projectsLastOneDay = getProjectsUpdatedInPastDays(1);
        sendEmailToEveryCompany(projectsLastOneDay);
    }

    private void sendEmailToEveryCompany(List<Project> projectsToCheck) {
        List<Company> companies = companyRepository.findAll();

        for (Company c : companies) {
            Set<Skill> skills = getAllEnabledCompanyFilterSkills(c);
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
