package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Request.RequiredSkillsDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Pattern;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionStatus;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.Seniority;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Entity.WorkMode;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Repository.PatternRepository;
import com.example.b2b_opportunities.Repository.PositionRepository;
import com.example.b2b_opportunities.Repository.PositionStatusRepository;
import com.example.b2b_opportunities.Repository.ProjectRepository;
import com.example.b2b_opportunities.Repository.SeniorityRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import com.example.b2b_opportunities.Repository.WorkModeRepository;
import com.example.b2b_opportunities.Static.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class PositionServiceTest {

    @InjectMocks
    PositionService positionService;

    @Mock
    Authentication authentication;

    @Mock
    PositionRepository positionRepository;

    @Mock
    UserService userService;

    @Mock
    CompanyService companyService;

    @Mock
    PatternRepository patternRepository;

    @Mock
    SeniorityRepository seniorityRepository;
    @Mock
    SkillRepository skillRepository;

    @Mock
    WorkModeRepository workModeRepository;
    @Mock
    ProjectRepository projectRepository;

    @Mock
    PositionStatusRepository positionStatusRepository;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldEditPositionStatusAndActivateProject(){
        User user = new User();
        Company company = new Company();

        Project project = new Project();
        project.setId(1L);
        project.setProjectStatus(ProjectStatus.INACTIVE);
        project.setCompany(company);

        company.setProjects(List.of(project));

        PositionStatus currentPositionStatus = new PositionStatus();
        currentPositionStatus.setId(2L);

        Position position = Position.builder()
                .id(1L)
                .project(project)
                .build();

        Pattern pattern = new Pattern();
        pattern.setId(5L);

        PositionStatus newPositionStatus = new PositionStatus();
        newPositionStatus.setId(1L);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionRepository.findById(anyLong())).thenReturn(Optional.of(position));
        when(positionStatusRepository.findById(anyLong())).thenReturn(Optional.of(newPositionStatus));

        positionService.editPositionStatus(1L, 1L, null, authentication);

        assertEquals(project.getProjectStatus(), ProjectStatus.ACTIVE);
        assertEquals(position.getStatus().getId(), 1L);
    }

    @Test
    void shouldThrowExceptionWhenNoCustomReasonIsPassedWhenClosing(){
        User user = new User();
        Company company = new Company();

        Project project = new Project();
        project.setId(1L);
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setCompany(company);

        company.setProjects(List.of(project));

        PositionStatus currentPositionStatus = new PositionStatus();
        currentPositionStatus.setId(2L);

        Position position = Position.builder()
                .id(1L)
                .project(project)
                .build();

        project.setPositions(List.of(position));

        Pattern pattern = new Pattern();
        pattern.setId(5L);

        PositionStatus newPositionStatus = new PositionStatus();
        newPositionStatus.setId(5L);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionRepository.findById(anyLong())).thenReturn(Optional.of(position));
        when(positionStatusRepository.findById(anyLong())).thenReturn(Optional.of(newPositionStatus));

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                positionService.editPositionStatus(1L, 5L, null, authentication));

        assertEquals(exception.getMessage(), "Custom close reason must be entered");
        assertEquals(exception.getField(), "customCloseReason");
    }

    @Test
    void shouldDeactivateProjectAndClosePosition(){
        User user = new User();
        Company company = new Company();

        Project project = new Project();
        project.setId(1L);
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setCompany(company);

        company.setProjects(List.of(project));

        PositionStatus currentPositionStatus = new PositionStatus();
        currentPositionStatus.setId(2L);

        Position position = Position.builder()
                .id(1L)
                .project(project)
                .build();

        project.setPositions(List.of(position));

        Pattern pattern = new Pattern();
        pattern.setId(5L);

        PositionStatus newPositionStatus = new PositionStatus();
        newPositionStatus.setId(5L);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionRepository.findById(anyLong())).thenReturn(Optional.of(position));
        when(positionStatusRepository.findById(anyLong())).thenReturn(Optional.of(newPositionStatus));

        positionService.editPositionStatus(1L, 5L, "testCloseReason", authentication);

        assertEquals(position.getCustomCloseReason(), "testCloseReason");
        assertEquals(position.getStatus().getId(), 5L);
        assertEquals(project.getProjectStatus(), ProjectStatus.INACTIVE);
    }

    @Test
    void shouldReturnListOfMissingWorkModesWhenNewPositionIsCreated(){
        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        PositionRequestDto dto = new PositionRequestDto();
        dto.setProjectId(1L);
        dto.setPatternId(1L);

        RequiredSkillsDto requiredSkillsDto = new RequiredSkillsDto();
        requiredSkillsDto.setSkillId(1L);

        Skill skill = new Skill();
        skill.setId(1L);
        skill.setAssignable(true);

        dto.setRequiredSkills(List.of(requiredSkillsDto));
        dto.setSeniority(1L);
        dto.setWorkMode(List.of(1L,2L));

        WorkMode wm = new WorkMode();
        wm.setId(1L);

        Project project = new Project();
        project.setId(1L);
        project.setCompany(company);
        Pattern pattern = new Pattern();
        Seniority seniority = new Seniority();

        company.setProjects(List.of(project));

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(patternRepository.findById(anyLong())).thenReturn(Optional.of(pattern));
        when(skillRepository.findById(any())).thenReturn(Optional.of(skill));
        when(seniorityRepository.findById(anyLong())).thenReturn(Optional.of(seniority));

        when(workModeRepository.findAllById(anyList())).thenReturn(List.of(wm));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                positionService.createPosition(dto, authentication)
        );

        assertEquals(exception.getMessage(), "WorkMode with ID(s): [2] not found");
    }
}
