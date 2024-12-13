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
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Repository.PositionApplicationRepository;
import com.example.b2b_opportunities.Repository.PositionRepository;
import com.example.b2b_opportunities.Repository.TalentRepository;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import com.example.b2b_opportunities.Static.ProjectStatus;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PositionApplicationServiceTest {

    @InjectMocks
    PositionApplicationService positionApplicationService;

    @Mock
    UserService userService;

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
    PositionApplicationRepository positionApplicationRepository;

    @Mock
    MinioClient minioClient;

    @Mock
    Authentication authentication;

    @Mock
    MultipartFile multipartFile;

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
        Position position = mock(Position.class);
        Project project = mock(Project.class);
        PositionApplication application = new PositionApplication();
        application.setId(1L);
        position.setId(1L);
        application.setPosition(position);
        application.setApplicationStatus(ApplicationStatus.ACCEPTED);

        when(userService.getCurrentUserOrThrow(authentication)).thenReturn(user);
        when(companyService.getUserCompanyOrThrow(user)).thenReturn(company);
        when(positionService.getPositionOrThrow(requestDto.getPositionId())).thenReturn(position);
        when(position.getProject()).thenReturn(project);
        when(positionApplicationRepository.save(any(PositionApplication.class))).thenReturn(application);

        PositionApplicationResponseDto response = positionApplicationService.applyForPosition(authentication, requestDto);

        assertNotNull(response);
        verify(positionApplicationRepository).save(any(PositionApplication.class));
    }

    @Test
    void testUploadCV() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
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

        String url = positionApplicationService.uploadCV(file, 1L);

        assertNotNull(url);
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(inputStream).close();
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
        verify(positionApplicationRepository).save(any(PositionApplication.class));
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
        verify(positionApplicationRepository).save(any(PositionApplication.class));
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
        verify(positionApplicationRepository, times(3)).save(application);
        verify(inputStream).close();
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
        verify(positionApplicationRepository).save(any(PositionApplication.class));
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
        Project project = mock(Project.class);
        Position position = mock(Position.class);
        PositionApplication application = new PositionApplication();
        application.setApplicationStatus(ApplicationStatus.ACCEPTED);
        application.setPosition(position);

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
        Assertions.assertFalse(response.isEmpty());
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
}