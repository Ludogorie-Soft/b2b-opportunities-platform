package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.dto.responseDtos.CompanyResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.UserSummaryDto;
import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.mapper.CompanyMapper;
import com.example.b2b_opportunities.mapper.UserMapper;
import com.example.b2b_opportunities.repository.CompanyRepository;
import com.example.b2b_opportunities.repository.UserRepository;
import com.example.b2b_opportunities.services.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
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

    @Override
    public List<CompanyResponseDto> getAllCompaniesData() {
        return CompanyMapper.toCompanyResponseDtoList(companyRepository.findAll());
    }

    @Override
    public Page<UserSummaryDto> getUsersSummary(int offset, int pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        return userRepository.findAllUsers(pageable).map(UserMapper::toSummaryDto);
    }
}