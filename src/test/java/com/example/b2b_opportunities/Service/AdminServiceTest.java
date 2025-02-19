package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.BaseTest;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Service.Implementation.AdminServiceImpl;
import com.example.b2b_opportunities.Service.Interface.AdminService;
import com.example.b2b_opportunities.Service.Interface.EmailSchedulerService;
import com.example.b2b_opportunities.Static.EmailVerification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AdminServiceTest extends BaseTest {
    @MockitoBean
    private EmailSchedulerService emailSchedulerService;

    private final CompanyRepository companyRepository;
    private final CompanyTypeRepository companyTypeRepository;
    private final AdminService adminService;
    private CompanyType companyType;

    @Autowired
    public AdminServiceTest(CompanyRepository companyRepository, AdminServiceImpl adminService, CompanyTypeRepository companyTypeRepository) {
        this.companyRepository = companyRepository;
        this.adminService = adminService;
        this.companyTypeRepository = companyTypeRepository;
    }

    @BeforeEach
    void setup() {
        companyType = CompanyType.builder()
                .name("testType")
                .build();
    }

    @Test
    void shouldApproveUnapprovedCompany() {
        companyType = companyTypeRepository.save(companyType);

        Company company = companyRepository.save(Company.builder()
                .companyType(companyType)
                .name("test")
                .email("test@test.test")
                .isApproved(false)
                .emailVerification(EmailVerification.ACCEPTED)
                .skills(new HashSet<>())
                .build());

        CompanyResponseDto approvedCompany = adminService.approve(company.getId());

        assertNotNull(approvedCompany);
        assertTrue(approvedCompany.isApproved());
        Company updatedCompany = companyRepository.findById(company.getId()).orElseThrow();
        assertTrue(updatedCompany.isApproved());
    }

    @Test
    void shouldNotChangeAlreadyApprovedUser() {
        companyType = companyTypeRepository.save(companyType);

        Company company = companyRepository.save(Company.builder()
                .companyType(companyType)
                .name("test")
                .email("test@test.test")
                .isApproved(false)
                .emailVerification(EmailVerification.ACCEPTED)
                .skills(new HashSet<>())
                .build());

        CompanyResponseDto approvedCompany = adminService.approve(company.getId());

        assertNotNull(approvedCompany);
        assertTrue(approvedCompany.isApproved());
        Company updatedCompany = companyRepository.findById(company.getId()).orElseThrow();
        assertTrue(updatedCompany.isApproved());
    }

    @Test
    void shouldGetAllNonApprovedCompanies() {
        companyRepository.deleteAll();
        companyType = companyTypeRepository.save(companyType);

        Company company1 = companyRepository.save(Company.builder()
                .companyType(companyType)
                .name("test1")
                .email("test1@test.test")
                .isApproved(false)
                .emailVerification(EmailVerification.ACCEPTED)
                .skills(new HashSet<>())
                .build());

        Company company2 = companyRepository.save(Company.builder()
                .companyType(companyType)
                .name("test2")
                .email("test2@test.test")
                .isApproved(true) // Approved - to make sure it's not being count
                .emailVerification(EmailVerification.ACCEPTED)
                .skills(new HashSet<>())
                .build());

        Company company3 = companyRepository.save(Company.builder()
                .companyType(companyType)
                .name("test3")
                .email("test3@test.test")
                .isApproved(false)
                .emailVerification(EmailVerification.ACCEPTED)
                .skills(new HashSet<>())
                .build());

        companyRepository.saveAll(List.of(company1, company2, company3));

        List<CompanyResponseDto> nonApprovedCompanies = adminService.getAllNonApprovedCompanies();

        assertNotNull(nonApprovedCompanies);
        assertEquals(2, nonApprovedCompanies.size()); // companies 1 and 3
        assertTrue(nonApprovedCompanies.stream().noneMatch(CompanyResponseDto::isApproved));
    }
}