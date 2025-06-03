package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.dto.responseDtos.CompanyResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.UserSummaryDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminService {
    CompanyResponseDto approve(Long id);

    List<CompanyResponseDto> getAllNonApprovedCompanies();

    List<CompanyResponseDto> getAllCompaniesData();

    Page<UserSummaryDto> getUsersSummary(int page, int size);
}
