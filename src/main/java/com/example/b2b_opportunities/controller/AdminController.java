package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.dto.responseDtos.CompanyResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.TalentStatsDto;
import com.example.b2b_opportunities.dto.responseDtos.UserSummaryDto;
import com.example.b2b_opportunities.services.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/approve/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyResponseDto approveCompany(@PathVariable("id") Long id) {
        return adminService.approve(id);
    }

    @GetMapping("/get-non-approved")
    @ResponseStatus(HttpStatus.OK)
    public List<CompanyResponseDto> getAllNonApprovedCompanies() {
        return adminService.getAllNonApprovedCompanies();
    }

    @GetMapping("/get-companies")
    @ResponseStatus(HttpStatus.OK)
    public List<CompanyResponseDto> getAllCompaniesData() {
        return adminService.getAllCompaniesData();
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public Page<UserSummaryDto> getUsersSummary(@RequestParam(defaultValue = "0") int offset,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return adminService.getUsersSummary(offset, pageSize);
    }

    @GetMapping("/talent-stats")
    @ResponseStatus(HttpStatus.OK)
    public Page<TalentStatsDto> getTalentStats(@RequestParam(defaultValue = "0") int offset,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return adminService.getTalentStats(offset, pageSize);
    }
}