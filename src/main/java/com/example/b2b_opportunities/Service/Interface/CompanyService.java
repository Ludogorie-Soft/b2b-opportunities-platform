package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Dto.Request.*;
import com.example.b2b_opportunities.Dto.Response.*;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface CompanyService {

    CompanyResponseDto createCompany(Authentication authentication, CompanyRequestDto companyRequestDto, HttpServletRequest request);

    CompanyResponseDto getCompany(Long companyId);

    CompaniesAndUsersResponseDto getCompanyAndUsers(Long companyId);

    void confirmCompanyEmail(String token);

    CompanyResponseDto editCompany(Authentication authentication,
                                   CompanyRequestDto companyRequestDto,
                                   HttpServletRequest request);

    CompanyResponseDto setCompanyImages(Authentication authentication, MultipartFile image, MultipartFile banner);

    void deleteCompanyBanner(Authentication authentication);

    void deleteCompanyImage(Authentication authentication);

    Set<ProjectResponseDto> getCompanyProjects(Authentication authentication, Long companyId);

    List<CompanyFilterResponseDto> getCompanyFilters(Authentication authentication);

    CompanyFilterResponseDto getCompanyFilter(Long id, Authentication authentication);

    CompanyFilterResponseDto editCompanyFilter(Long id, CompanyFilterEditDto dto, Authentication authentication);

    void deleteCompanyFilter(Long id, Authentication authentication);

    CompanyFilterResponseDto addCompanyFilter(Authentication authentication, @Valid CompanyFilterRequestDto dto);

    List<PartnerGroupResponseDto> getPartnerGroups(Authentication authentication);

    PartnerGroupResponseDto removeCompanyFromPartners(Authentication authentication, Long partnerGroupId, Long companyId);

    void deletePartnerGroup(Authentication authentication, Long partnerGroupId);

    List<CompanyPublicResponseDto> getAcceptedCompaniesPublicData();

    PartnerGroupResponseDto createPartnerGroup(Authentication authentication, PartnerGroupRequestDto dto);

    PartnerGroupResponseDto editPartnerGroup(Authentication authentication, Long partnerGroupId, PartnerGroupRequestDto dto);

    TalentResponseDto createTalent(Authentication authentication, TalentRequestDto talentRequestDto);

    TalentResponseDto updateTalent(Authentication authentication, Long talentId, TalentRequestDto talentRequestDto);

    Page<TalentResponseDto> getAllTalents(Authentication authentication, Pageable pageable);

    TalentResponseDto getTalentById(Authentication authentication, Long talentId);

    List<TalentResponseDto> getMyTalents(Authentication authentication);

    void deleteTalent(Authentication authentication, Long id);

    void setTalentVisibility(Authentication authentication, TalentPublicityRequestDto requestDto);

    TalentPublicityResponseDto getTalentVisibility(Authentication authentication);

    Company getCompanyOrThrow(Long id);

    Company getUserCompanyOrThrow(User user);

    Talent getTalentOrThrow(Long talentId);
}
