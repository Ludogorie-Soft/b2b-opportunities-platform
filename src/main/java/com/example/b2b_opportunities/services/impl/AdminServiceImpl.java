package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.dto.responseDtos.CompanyResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.ProjectStatsDto;
import com.example.b2b_opportunities.dto.responseDtos.TalentStatsDto;
import com.example.b2b_opportunities.dto.responseDtos.UserSummaryDto;
import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.EmailDailyStats;
import com.example.b2b_opportunities.entity.Project;
import com.example.b2b_opportunities.entity.Talent;
import com.example.b2b_opportunities.enums.ProjectStatus;
import com.example.b2b_opportunities.mapper.CompanyMapper;
import com.example.b2b_opportunities.mapper.ProjectMapper;
import com.example.b2b_opportunities.mapper.TalentMapper;
import com.example.b2b_opportunities.mapper.UserMapper;
import com.example.b2b_opportunities.repository.CompanyRepository;
import com.example.b2b_opportunities.repository.EmailDailyStatsRepository;
import com.example.b2b_opportunities.repository.PositionApplicationRepository;
import com.example.b2b_opportunities.repository.ProjectRepository;
import com.example.b2b_opportunities.repository.TalentRepository;
import com.example.b2b_opportunities.repository.UserRepository;
import com.example.b2b_opportunities.services.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyServiceImpl companyService;
    private final TalentRepository talentRepository;
    private final PositionApplicationRepository positionApplicationRepository;
    private final ProjectRepository projectRepository;
    private final EmailDailyStatsRepository emailDailyStatsRepository;

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

    @Override
    public Page<TalentStatsDto> getTalentStats(int offset, int pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("createdAt").descending());
        Page<Talent> talentsPage = talentRepository.findAll(pageable);
        List<TalentStatsDto> dtoList = talentsPage.stream().
                map(talent -> {
                    TalentStatsDto dto = TalentMapper.toTalentStatsDto(talent);
                    long applicationCount = positionApplicationRepository.countByTalentId(talent.getId());
                    dto.setPositionsApplied((int) applicationCount);
                    return dto;
                }).toList();
        return new PageImpl<>(dtoList, pageable, talentsPage.getTotalElements());
    }

    @Override
    public Page<ProjectStatsDto> getProjectStats(int offset, int pageSize, boolean active) {
        if (active) return getProjects(offset, pageSize, ProjectStatus.ACTIVE);
        else return getProjects(offset, pageSize, ProjectStatus.INACTIVE);
    }

    @Override
    public Page<EmailDailyStats> getDailyEmailStats(int offset, int pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        return emailDailyStatsRepository.findAllByOrderByDayDesc(pageable);
    }

    private Page<ProjectStatsDto> getProjects(int offset, int pageSize, ProjectStatus projectStatus) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Project> projects = projectRepository.findProjectsByStatus(projectStatus, pageable);
        return projects.map(
                project -> {
                    Long applicationCount = positionApplicationRepository.countByProjectId(project.getId());
                    LocalDateTime lastApplicationAt = positionApplicationRepository.findLastApplicationDateTimeByProjectId(project.getId());
                    long emailsSent = companyRepository.countCompaniesThatWereNotifiedForProject(project.getId());
                    ProjectStatsDto projectStatsDto = ProjectMapper.toProjectStatsDto(project);
                    projectStatsDto.setApplicationCount(applicationCount);
                    projectStatsDto.setLastApplicationAt(lastApplicationAt);
                    projectStatsDto.setEmailsSent(emailsSent);
                    return projectStatsDto;
                });
    }

}