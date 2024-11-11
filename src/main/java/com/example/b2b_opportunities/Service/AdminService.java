package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final CompanyRepository companyRepository;

    public CompanyResponseDto approve(Long id) {
        Company company = companyRepository.findById(id).orElseThrow(() -> new NotFoundException("Company with id " + id + " not found"));
        if (company.isApproved()) {
            return CompanyMapper.toCompanyResponseDto(company);
        }
        company.setApproved(true);
        return CompanyMapper.toCompanyResponseDto(companyRepository.save(company));
    }

    public List<CompanyResponseDto> getAllNonApprovedCompanies() {
        List<Company> companies = companyRepository.findByIsApprovedFalse();
        return CompanyMapper.toCompanyResponseDtoList(companies);
    }
}
