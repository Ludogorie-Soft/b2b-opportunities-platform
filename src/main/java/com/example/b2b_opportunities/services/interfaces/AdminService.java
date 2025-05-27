package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.dto.responseDtos.CompanyResponseDto;

import java.util.List;

public interface AdminService {
    CompanyResponseDto approve(Long id);

    List<CompanyResponseDto> getAllNonApprovedCompanies();

    List<CompanyResponseDto> getAllCompaniesData();
}
