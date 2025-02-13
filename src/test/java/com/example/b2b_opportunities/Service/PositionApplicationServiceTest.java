package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.PositionApplicationRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionApplicationResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Entity.PositionStatus;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.ServerErrorException;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Repository.PositionApplicationRepository;
import com.example.b2b_opportunities.Repository.PositionRepository;
import com.example.b2b_opportunities.Repository.TalentRepository;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import com.example.b2b_opportunities.Static.ProjectStatus;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PositionApplicationServiceTest {

    @InjectMocks
    PositionApplicationService positionApplicationService;

    @Mock
    UserService userService;

    @Mock
    ImageService imageService;

    @Mock
    CompanyService companyService;

    @Mock
    ProjectService projectService;

    @Mock
    PositionService positionService;

    @Mock
    TalentRepository talentRepository;

    @Mock
    PositionRepository positionRepository;

    @Mock
    LocalDateTime localDateTime;

    @Mock
    PositionApplicationRepository positionApplicationRepository;

    @Mock
    MinioClient minioClient;

    @Mock
    Authentication authentication;

    @Mock
    MultipartFile multipartFile;

    @Mock
    MailService mailService;

    private PositionApplicationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authentication = mock(Authentication.class);
        requestDto = new PositionApplicationRequestDto();
        ReflectionTestUtils.setField(positionApplicationService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(positionApplicationService, "storageUrl", "http://localhost:9000");
    }

    @Test
    void testApplyForPosition() {
        User user = mock(User.class);
        Company company = mock(Company.class);
        Company userCompany = mock(Company.class);
        Position position = mock(Position.class);
        Project project = mock(Project.class);
        PositionApplication application = new PositionApplication();
        application.setId(1L);
        position.setId(1L);
        application.setPosition(position);
        application.setApplicationStatus(ApplicationStatus.IN_PROGRESS);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(project.getProjectStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(position.getStatus()).thenReturn(PositionStatus.builder().id(1L).build());
        when(user.getCompany()).thenReturn(userCompany);
        when(userCompany.getId()).thenReturn(2L);
        when(project.getCompany()).thenReturn(company);
        when(company.getId()).thenReturn(1L);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(userCompany);
        when(positionService.getPositionOrThrow(requestDto.getPositionId())).thenReturn(position);
        when(position.getProject()).thenReturn(project);
        when(positionApplicationRepository.save(any(PositionApplication.class))).thenReturn(application);

        PositionApplicationResponseDto response = positionApplicationService.applyForPosition(authentication, requestDto);

        assertEquals(response.getApplicationStatus(), ApplicationStatus.IN_PROGRESS.toString());
        assertNotNull(response);
    }

    @Test
    void testUploadCV() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        InputStream inputStream = mock(InputStream.class);
        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");

        Position position = new Position();
        position.setId(1L);

        PositionApplication application = new PositionApplication();
        application.setId(1L);
        application.setPosition(position);

        when(positionApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        PositionApplicationResponseDto responseDto = positionApplicationService.uploadCV(file, 1L);

        assertNotNull(responseDto);
    }

    @Test
    void testDoesCVExist() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(null);

        boolean exists = positionApplicationService.doesCVExist(1L);

        assertTrue(exists);
    }

    @Test
    void testReturnUrlIfCVExists() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(null);

        String url = positionApplicationService.returnUrlIfCVExists(1L);

        assertNotNull(url);
    }

    @Test
    void testGetApplicationsForMyPositions() {
        User user = new User();
        Company company = mock(Company.class);
        Project project = mock(Project.class);
        Position position = mock(Position.class);
        List<Project> projects = List.of(project);
        List<Position> positions = List.of(position);
        List<PositionApplication> applications = new ArrayList<>();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(company.getProjects()).thenReturn(projects);
        when(project.getPositions()).thenReturn(positions);
        when(positionApplicationRepository.findAllApplicationsForMyPositions(anyLong(), any())).thenReturn(applications);

        List<PositionApplicationResponseDto> response = positionApplicationService.getApplicationsForMyPositions(authentication);

        assertNotNull(response);
    }

    @Test
    void testGetMyApplications() {
        User user = new User();
        Company company = new Company();
        List<PositionApplication> applications = new ArrayList<>();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findAllMyApplications(anyLong())).thenReturn(applications);

        List<PositionApplicationResponseDto> response = positionApplicationService.getMyApplications(authentication);

        assertNotNull(response);
        Assertions.assertEquals(response.size(), 0);
    }

    @Test
    void testGetMyApplicationsWithTwoApplications() {
        User user = new User();
        Company company = new Company();
        List<PositionApplication> applications = new ArrayList<>();
        Position position = Position.builder().id(99999999L).build();
        PositionApplication paOne = PositionApplication.builder().id(999999999998L).position(position).applicationStatus(ApplicationStatus.IN_PROGRESS).build();
        PositionApplication paTwo = PositionApplication.builder().id(999999999999L).position(position).applicationStatus(ApplicationStatus.IN_PROGRESS).build();
        applications.add(paOne);
        applications.add(paTwo);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findAllMyApplications(company.getId())).thenReturn(applications);

        List<PositionApplicationResponseDto> response = positionApplicationService.getMyApplications(authentication);

        assertNotNull(response);
        Assertions.assertEquals(2, response.size());
        for (PositionApplicationResponseDto dto : response) {
            Assertions.assertEquals(dto.getCvUrl(), "http://localhost:9000/test-bucket/CV/" + dto.getId());
        }
    }

    @Test
    void shouldThrowInvalidRequestExceptionWhenApplyingToOwnCompanyPosition() {
        User user = new User();
        Company company = new Company();
        company.setId(9999999999L);
        Position position = Position.builder().id(99999999L).build();
        Project project = new Project();
        project.setId(999999999L);
        position.setProject(project);
        project.setCompany(company);
        user.setCompany(company);
        PositionApplicationRequestDto dto = new PositionApplicationRequestDto();
        dto.setPositionId(position.getId());
        dto.setRate(5);
        dto.setAvailableFrom(LocalDateTime.now());

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionService.getPositionOrThrow(anyLong())).thenReturn(position);

        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () ->
        {
            positionApplicationService.applyForPosition(authentication, dto);
        });

        assertEquals(exception.getMessage(), "You can't apply to a position that belongs to your company!", "positionId");
    }

    @Test
    void shouldThrowInvalidRequestExceptionWhenApplyingToInactiveProject() {
        User user = new User();
        Company company = new Company();
        company.setId(9999999999L);
        user.setCompany(company);

        Company projectCompany = new Company();
        projectCompany.setId(5555555555L);

        Position position = Position.builder().id(99999999L).build();
        Project project = new Project();
        project.setId(999999999L);
        project.setProjectStatus(ProjectStatus.INACTIVE);
        position.setProject(project);
        project.setCompany(projectCompany);

        PositionApplicationRequestDto dto = new PositionApplicationRequestDto();
        dto.setPositionId(position.getId());
        dto.setRate(5);
        dto.setAvailableFrom(LocalDateTime.now());

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionService.getPositionOrThrow(anyLong())).thenReturn(position);

        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () ->
        {
            positionApplicationService.applyForPosition(authentication, dto);
        });

        assertEquals(exception.getMessage(), "This position belongs to a project that is inactive", "positionId");
        verifyNoInteractions(positionApplicationRepository);
    }

    @Test
    void shouldThrowInvalidRequestExceptionWhenApplyingToClosedPosition() {
        User user = new User();
        Company company = new Company();
        company.setId(9999999999L);
        user.setCompany(company);

        Company projectCompany = new Company();
        projectCompany.setId(5555555555L);

        Position position = Position.builder().id(99999999L).build();
        Project project = new Project();
        project.setId(999999999L);

        //Inactive status
        PositionStatus ps = PositionStatus.builder().id(99L).build();
        position.setProject(project);
        position.setStatus(ps);
        project.setCompany(projectCompany);
        project.setProjectStatus(ProjectStatus.ACTIVE);

        PositionApplicationRequestDto dto = new PositionApplicationRequestDto();
        dto.setPositionId(position.getId());
        dto.setRate(5);
        dto.setAvailableFrom(LocalDateTime.now());

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionService.getPositionOrThrow(anyLong())).thenReturn(position);

        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () ->
        {
            positionApplicationService.applyForPosition(authentication, dto);
        });

        assertEquals(exception.getMessage(), "The position is not opened", "positionId");
        verifyNoInteractions(positionApplicationRepository);
    }

    @Test
    void testAcceptApplication() {
        User user = new User();
        Company company = new Company();
        company.setId(1L);
        Position position = new Position();
        position.setId(1L);
        Project project = new Project();
        project.setCompany(company);
        position.setProject(project);
        PositionApplication application = new PositionApplication();
        application.setId(1L);
        application.setTalentCompany(company);
        application.setPosition(position);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(positionApplicationRepository.save(any(PositionApplication.class))).thenReturn(application);

        PositionApplicationResponseDto response = positionApplicationService.acceptApplication(authentication, 1L);

        assertNotNull(response);
    }

    @Test
    void testRejectApplication() {
        User user = new User();
        Company company = new Company();
        company.setId(1L);
        Position position = new Position();
        position.setId(1L);
        Project project = new Project();
        project.setCompany(company);
        position.setProject(project);
        PositionApplication application = new PositionApplication();
        application.setId(1L);
        application.setTalentCompany(company);
        application.setPosition(position);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(positionApplicationRepository.save(any(PositionApplication.class))).thenReturn(application);

        PositionApplicationResponseDto response = positionApplicationService.rejectApplication(authentication, 1L);

        assertNotNull(response);
    }

    @Test
    void testGetApplicationById() {
        User user = new User();
        Company company = new Company();
        company.setId(1L);
        Position position = new Position();
        position.setId(1L);
        Project project = new Project();
        project.setCompany(company);
        position.setProject(project);
        PositionApplication application = new PositionApplication();
        application.setId(1L);
        application.setTalentCompany(company);
        application.setPosition(position);
        application.setApplicationStatus(ApplicationStatus.ACCEPTED);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));

        PositionApplicationResponseDto response = positionApplicationService.getApplicationById(authentication, 1L);

        assertNotNull(response);
    }

    @Test
    void testUpdateApplication() throws IOException {
        User user = new User();
        Company company = new Company();
        company.setId(1L);
        Position position = new Position();
        position.setId(1L);
        PositionApplication application = new PositionApplication();
        application.setId(1L);
        application.setTalentCompany(company);
        application.setPosition(position);
        MultipartFile file = mock(MultipartFile.class);
        InputStream inputStream = mock(InputStream.class);
        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");
        Talent talent = new Talent();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(companyService.getTalentOrThrow(anyLong())).thenReturn(talent);

        PositionApplicationResponseDto response = positionApplicationService.updateApplication(authentication, file, 1L, 1L);

        assertNotNull(response);
    }

    @Test
    void testApplyForPositionWithTalent() {
        User user = mock(User.class);
        Company userCompany = mock(Company.class);
        Company projectCompany = mock(Company.class);
        Position position = mock(Position.class);
        Project project = mock(Project.class);
        Talent talent = mock(Talent.class);
        PositionStatus positionStatus = mock(PositionStatus.class);
        PositionApplication application = new PositionApplication();
        application.setId(1L);
        position.setId(1L);
        application.setPosition(position);
        application.setApplicationStatus(ApplicationStatus.ACCEPTED);
        requestDto.setTalentId(1L);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(userCompany);
        when(positionService.getPositionOrThrow(requestDto.getPositionId())).thenReturn(position);
        when(position.getProject()).thenReturn(project);
        when(position.getStatus()).thenReturn(positionStatus);
        when(positionStatus.getId()).thenReturn(1L);
        when(project.getCompany()).thenReturn(projectCompany);
        when(project.getProjectStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(companyService.getTalentOrThrow(requestDto.getTalentId())).thenReturn(talent);
        when(positionApplicationRepository.save(any(PositionApplication.class))).thenReturn(application);
        when(userCompany.getId()).thenReturn(1L);
        when(projectCompany.getId()).thenReturn(2L);

        PositionApplicationResponseDto response = positionApplicationService.applyForPosition(authentication, requestDto);

        assertNotNull(response);
    }

    @Test
    void testGetApplicationsForMyPositionsWithEmptyPositions() {
        User user = new User();
        Company company = mock(Company.class);
        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(company.getProjects()).thenReturn(new ArrayList<>());

        List<PositionApplicationResponseDto> response = positionApplicationService.getApplicationsForMyPositions(authentication);

        assertTrue(response.isEmpty());
    }

    @Test
    void testGetApplicationsForMyPositionsWithNonEmptyPositions() {
        User user = new User();
        Company company = mock(Company.class);
        company.setName("companyNameTest");
        Project project = mock(Project.class);
        Position position = mock(Position.class);
        PositionApplication application = new PositionApplication();
        application.setApplicationStatus(ApplicationStatus.ACCEPTED);
        application.setPosition(position);
        application.setTalentCompany(company);

        List<Project> projects = List.of(project);
        List<Position> positions = List.of(position);
        List<PositionApplication> applications = List.of(application);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(company.getProjects()).thenReturn(projects);
        when(project.getPositions()).thenReturn(positions);
        when(positionApplicationRepository.findAllApplicationsForMyPositions(anyLong(), any())).thenReturn(applications);

        List<PositionApplicationResponseDto> response = positionApplicationService.getApplicationsForMyPositions(authentication);

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testUpdateApplicationPermissionDenied() {
        User user = new User();
        Company company = new Company();
        company.setId(1L);
        PositionApplication application = new PositionApplication();
        application.setId(1L);
        Company otherCompany = new Company();
        otherCompany.setId(2L);
        application.setTalentCompany(otherCompany);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));

        assertThrows(PermissionDeniedException.class, () -> positionApplicationService.updateApplication(authentication, null, 1L, null));
    }

    @Test
    void testUpdatePositionApplicationStatusInvalidStatus() {
        User user = new User();
        Company company = new Company();
        company.setId(1L);
        Position position = new Position();
        position.setId(1L);
        Project project = new Project();
        project.setCompany(company);
        position.setProject(project);
        PositionApplication application = new PositionApplication();
        application.setId(1L);
        application.setTalentCompany(company);
        application.setPosition(position);
        application.setApplicationStatus(ApplicationStatus.DENIED);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));

        assertThrows(InvalidRequestException.class, () -> positionApplicationService.acceptApplication(authentication, 1L));
    }

    @Test
    void testGetApplicationByIdWithNoAccessShouldThrowPermissionDeniedException() {
        User user = new User();
        Company company = Company.builder().id(1L).build();

        Company anotherCompany = Company.builder().id(99999999L).build();

        Position position = new Position();
        position.setId(1L);
        Project project = new Project();
        project.setCompany(anotherCompany);
        position.setProject(project);

        PositionApplication pa = PositionApplication.builder()
                .id(9999999999L)
                .position(position)
                .applicationStatus(ApplicationStatus.IN_PROGRESS)
                .talentCompany(anotherCompany)
                .availableFrom(LocalDateTime.now())
                .build();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findById(anyLong())).thenReturn(Optional.of(pa));

        assertThrows(PermissionDeniedException.class, () ->
                {
                    positionApplicationService.getApplicationById(authentication, 9999999999L);
                }
        );
    }

    @Test
    void shouldNotChangeStatusWhenTargetStatusIsSameAsCurrent() {
        User user = new User();
        Company company = Company.builder().id(1L).build();

        Position position = new Position();
        position.setId(1L);
        Project project = new Project();
        project.setCompany(company);
        position.setProject(project);

        PositionApplication pa = PositionApplication.builder()
                .id(9999999999L)
                .position(position)
                .applicationStatus(ApplicationStatus.ACCEPTED)
                .talentCompany(company)
                .availableFrom(LocalDateTime.now())
                .build();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findById(anyLong())).thenReturn(Optional.of(pa));

        PositionApplicationResponseDto responseDto =
                positionApplicationService.acceptApplication(
                        authentication,
                        9999999999L);

        //only accessed once
//        verify(positionApplicationRepository, times(1));
        assertEquals(pa.getApplicationStatus(), ApplicationStatus.ACCEPTED);
    }

    @Test
    void shouldNotUpdatePositionApplicationStatusWhenItDoesNotBelongToCompany() {
        User user = new User();
        Company company = Company.builder().id(1L).build();

        Company anotherCompany = Company.builder().id(2L).build();

        Position position = new Position();
        position.setId(1L);
        Project project = new Project();
        project.setCompany(anotherCompany);
        position.setProject(project);

        PositionApplication pa = PositionApplication.builder()
                .id(9999999999L)
                .position(position)
                .talentCompany(company)
                .availableFrom(LocalDateTime.now())
                .build();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findById(anyLong())).thenReturn(Optional.of(pa));

        PermissionDeniedException exception = Assertions.assertThrows(PermissionDeniedException.class, () ->
        {
            positionApplicationService.acceptApplication(authentication, 9999999999L);
        });

        assertEquals(exception.getMessage(), "This application does not belong to your company");
    }

    @Test
    void shouldNotCreateAnotherApplicationWhenTheresApplicationInProgressAlready() {
        User user = new User();
        Company company = Company.builder().id(1L).build();

        Company anotherCompany = Company.builder().id(2L).build();

        Position position = new Position();
        position.setId(1L);
        position.setStatus(PositionStatus.builder().id(1L).build());
        Project project = new Project();
        project.setId(1L);
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setCompany(anotherCompany);
        position.setProject(project);

        Talent t = Talent.builder()
                .id(1L)
                .company(company)
                .isActive(true)
                .build();

        PositionApplication pa = PositionApplication
                .builder()
                .id(1L)
                .talent(t)
                .position(position)
                .rate(5)
                .applicationStatus(ApplicationStatus.IN_PROGRESS)
                .talentCompany(t.getCompany())
                .availableFrom(LocalDateTime.now())
                .build();

        PositionApplicationRequestDto dto = PositionApplicationRequestDto
                .builder()
                .positionId(1L)
                .talentId(t.getId())
                .rate(5)
                .build();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findFirstByPositionIdAndTalentIdAndApplicationStatusIn(anyLong(), anyLong(), anyList())).thenReturn(Optional.of(pa));
        ;
        when(positionService.getPositionOrThrow(1L)).thenReturn(position);
        when(companyService.getTalentOrThrow(anyLong())).thenReturn(t);

        AlreadyExistsException exception = Assertions.assertThrows(
                AlreadyExistsException.class, () -> {
                    positionApplicationService.applyForPosition(
                            authentication, dto);
                }
        );

        assertEquals(exception.getMessage(), "You've already applied for this position", "positionId");
    }

    @Test
    void shouldNotCreateAnotherApplicationWhenTheresApplicationAlreadyAccepted() {
        User user = new User();
        Company company = Company.builder().id(1L).build();

        Company anotherCompany = Company.builder().id(2L).build();

        Position position = new Position();
        position.setId(1L);
        position.setStatus(PositionStatus.builder().id(1L).build());
        Project project = new Project();
        project.setId(1L);
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setCompany(anotherCompany);
        position.setProject(project);

        Talent t = Talent.builder()
                .id(1L)
                .company(company)
                .isActive(true)
                .build();

        PositionApplication pa = PositionApplication
                .builder()
                .id(1L)
                .talent(t)
                .position(position)
                .rate(5)
                .applicationStatus(ApplicationStatus.ACCEPTED)
                .talentCompany(t.getCompany())
                .availableFrom(LocalDateTime.now())
                .build();

        PositionApplicationRequestDto dto = PositionApplicationRequestDto
                .builder()
                .positionId(1L)
                .talentId(t.getId())
                .rate(5)
                .build();

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionApplicationRepository.findFirstByPositionIdAndTalentIdAndApplicationStatusIn(anyLong(), anyLong(), anyList())).thenReturn(Optional.of(pa));
        ;
        when(positionService.getPositionOrThrow(1L)).thenReturn(position);
        when(companyService.getTalentOrThrow(anyLong())).thenReturn(t);

        AlreadyExistsException exception = Assertions.assertThrows(
                AlreadyExistsException.class, () -> {
                    positionApplicationService.applyForPosition(
                            authentication, dto);
                }
        );

        assertEquals(exception.getMessage(), "You're already accepted for this position", "positionId");
    }

    @Test
    void whenUploadingCVFailsShouldThrowServerErrorException() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenThrow(new IOException("IO error"));

        assertThrows(ServerErrorException.class, () -> {
            positionApplicationService.uploadCV(mockFile, 123L);
        });
    }

    @Test
    void shouldReturnFalseWhenObjectDoesNotExist() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        ErrorResponse errorResponse = new ErrorResponse("NoSuchKey", "Object not found", "test", "text/plain", null, null, null);
        doThrow(new ErrorResponseException(errorResponse, null, null))
                .when(minioClient).statObject(any(StatObjectArgs.class));

        boolean result = positionApplicationService.doesCVExist(1L);

        assertFalse(result);
    }

    @Test
    void shouldThrowServerErrorExceptionWhenMinioThrowsOtherErrorResponse() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("UnexpectedError", "message", null, null, null, null, null);
        doThrow(new ErrorResponseException(errorResponse, null, null))
                .when(minioClient).statObject(any(StatObjectArgs.class));

        ServerErrorException exception = Assertions.assertThrows(ServerErrorException.class, () -> {
            positionApplicationService.doesCVExist(1L);
        });

        assertTrue(exception.getMessage().contains("Error occurred while checking CV file for ID:"));
    }

    @Test
    void shouldThrowServerErrorExceptionWhenMinioThrowsOtherExceptions() throws Exception {
        doThrow(new IOException("I/O error occurred"))
                .when(minioClient).statObject(any(StatObjectArgs.class));

        ServerErrorException exception = Assertions.assertThrows(ServerErrorException.class, () -> {
            positionApplicationService.doesCVExist(1L);
        });

        assertTrue(exception.getMessage().contains("Error occurred while checking object existence:"));
    }

    @Test
    void testGetApplicationsSinceLastWorkday(){
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            LocalDate localDate = LocalDate.of(2025,2,3);
            mockedStatic.when(LocalDate::now).thenReturn(localDate);

            when(positionApplicationRepository.findAllApplicationsBetween(any(), any()))
                    .thenReturn(List.of(new PositionApplication(), new PositionApplication()));

            List<PositionApplication> result = positionApplicationService.getApplicationsSinceLastWorkday();

            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }
}
