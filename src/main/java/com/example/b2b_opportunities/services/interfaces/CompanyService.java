package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.dto.requestDtos.CompanyFilterEditDto;
import com.example.b2b_opportunities.dto.requestDtos.CompanyFilterRequestDto;
import com.example.b2b_opportunities.dto.requestDtos.CompanyRequestDto;
import com.example.b2b_opportunities.dto.requestDtos.PartnerGroupRequestDto;
import com.example.b2b_opportunities.dto.requestDtos.TalentPublicityRequestDto;
import com.example.b2b_opportunities.dto.requestDtos.TalentRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.CompanyFilterResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.CompanyPublicResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.CompanyResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.PartialPositionResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.PartialTalentResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.PartnerGroupResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.ProjectResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.TalentPublicityResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.TalentResponseDto;
import com.example.b2b_opportunities.entity.Company;
import com.example.b2b_opportunities.entity.Talent;
import com.example.b2b_opportunities.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

    Page<ProjectResponseDto> getCompanyProjects(Authentication authentication,
                                               Long companyId,
                                               int offset,
                                               int pageSize,
                                               String sort,
                                               boolean ascending);

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

    Page<TalentResponseDto> getAllTalents(Authentication authentication,
                                          int offset,
                                          int pageSize,
                                          String sort,
                                          Boolean ascending,
                                          Integer rate,
                                          Set<Long> workModes,
                                          Set<Long> skills);

    TalentResponseDto getTalentById(Authentication authentication, Long talentId);

    Page<TalentResponseDto> getMyTalents(Authentication authentication, int offset, int pageSize, String sort, Boolean ascending);

    void deleteTalent(Authentication authentication, Long id);

    void setTalentVisibility(Authentication authentication, TalentPublicityRequestDto requestDto);

    TalentPublicityResponseDto getTalentVisibility(Authentication authentication);

    Company getCompanyOrThrow(Long id);

    Company getUserCompanyOrThrow(User user);

    Talent getTalentOrThrow(Long talentId);

    List<PartialTalentResponseDto> getMyTalentsPartial(Authentication authentication);

    List<PartialPositionResponseDto> getMyPositionsPartial(Authentication authentication);

    CompanyPublicResponseDto getPublicCompanyInfoById(Long id);

    CompanyResponseDto resendConfirmationEmail(Authentication authentication, HttpServletRequest request);
}
