package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;

import java.util.List;

public interface AdminService {
    CompanyResponseDto approve(Long id);

    List<CompanyResponseDto> getAllNonApprovedCompanies();

    List<CompanyResponseDto> getAllCompaniesData();
}
