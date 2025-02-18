package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;

import java.util.List;

public interface AdminService {
    public CompanyResponseDto approve(Long id);
    public List<CompanyResponseDto> getAllNonApprovedCompanies();
}
