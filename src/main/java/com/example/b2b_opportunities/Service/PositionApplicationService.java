package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.PositionApplicationRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionApplicationResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Mapper.PositionMapper;
import com.example.b2b_opportunities.Repository.PositionApplicationRepository;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import com.example.b2b_opportunities.Static.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PositionApplicationService {
    private final UserService userService;
    private final CompanyService companyService;
    private final ProjectService projectService;
    private final PositionService positionService;
    private final PositionApplicationRepository positionApplicationRepository;

    public PositionApplicationResponseDto applyForPosition(Authentication authentication, PositionApplicationRequestDto requestDto) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = companyService.getUserCompanyOrThrow(user);
        Position position = positionService.getPositionOrThrow(requestDto.getPositionId());
        Project project = position.getProject();
        Talent talent = companyService.getTalentOrThrow(requestDto.getTalentId());

        companyService.validateTalentBelongsToCompany(userCompany, talent);
        if (project.getCompany().getId().equals(userCompany.getId())) {
            throw new InvalidRequestException("You can't apply to a position that belongs to your company!");
        }
        if (project.getProjectStatus().equals(ProjectStatus.INACTIVE)) {
            throw new InvalidRequestException("This position belongs to a project that is inactive");
        }
        projectService.validateProjectIsAvailableToCompany(project, userCompany);
        if (!position.getStatus().getId().equals(1L)) {
            throw new InvalidRequestException("The position is not opened");
        }

        //TODO do we want only one PA per talent or only one PA per company? (for 1 position)
        if (positionApplicationRepository.existsByPositionIdAndTalentIdAndApplicationStatus(position.getId(), requestDto.getTalentId(), ApplicationStatus.IN_PROGRESS)) {
            throw new AlreadyExistsException("You've already applied for this position");
        }

        PositionApplication application = PositionApplication.builder()
                .talent(talent)
                .position(position)
                .applicationStatus(ApplicationStatus.IN_PROGRESS)
                .applicationDateTime(LocalDateTime.now())
                .build();

        return PositionMapper.toPositionApplicationResponseDto(positionApplicationRepository.save(application));
    }
}
