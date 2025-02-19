package com.example.b2b_opportunities.Service.Implementation;

import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Service.Interface.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final CompanyRepository companyRepository;
    private final CompanyServiceImpl companyService;

    @Override
    public CompanyResponseDto approve(Long id) {
        Company company = companyService.getCompanyOrThrow(id);
        if (company.isApproved()) {
            log.info("Company {} (id = {}) already approved.", company.getName(), company.getId());
            return CompanyMapper.toCompanyResponseDto(company);
        }
        company.setApproved(true);
        log.info("Company {} (id = {}) approved.", company.getName(), company.getId());
        return CompanyMapper.toCompanyResponseDto(companyRepository.save(company));
    }

    @Override
    public List<CompanyResponseDto> getAllNonApprovedCompanies() {
        List<Company> companies = companyRepository.findByIsApprovedFalse();
        return CompanyMapper.toCompanyResponseDtoList(companies);
    }
}
