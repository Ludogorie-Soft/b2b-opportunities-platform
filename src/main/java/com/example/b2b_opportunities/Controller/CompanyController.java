package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.CompanyFilterEditDto;
import com.example.b2b_opportunities.Dto.Request.CompanyFilterRequestDto;
import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Request.PartnerGroupRequestDto;
import com.example.b2b_opportunities.Dto.Request.PositionApplicationRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentPublicityRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyFilterResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyPublicResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Dto.Response.PartialTalentResponseDto;
import com.example.b2b_opportunities.Dto.Response.PartnerGroupResponseDto;
import com.example.b2b_opportunities.Dto.Response.PositionApplicationResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentPublicityResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentResponseDto;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Service.Interface.CompanyService;
import com.example.b2b_opportunities.Service.Interface.PositionApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyRepository companyRepository;
    private final CompanyService companyService;
    private final PositionApplicationService positionApplicationService;

    @Value("${frontend.address}")
    private String frontEndAddress;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompanyPublicResponseDto> getAcceptedCompaniesPublicData() {
        return companyService.getAcceptedCompaniesPublicData();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyResponseDto getCompany(@PathVariable("id") Long id) {
        return companyService.getCompany(id);
    }

    @GetMapping("{id}/projects")
    @ResponseStatus(HttpStatus.OK)
    public Set<ProjectResponseDto> getCompanyProjects(Authentication authentication, @PathVariable("id") Long id) {
        return companyService.getCompanyProjects(authentication, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponseDto createCompany(Authentication authentication,
                                            @RequestBody @Valid CompanyRequestDto companyRequestDto,
                                            HttpServletRequest request) {
        return companyService.createCompany(authentication, companyRequestDto, request);
    }

    @GetMapping("/partners")
    @ResponseStatus(HttpStatus.OK)
    public List<PartnerGroupResponseDto> getPartnerGroups(Authentication authentication) {
        return companyService.getPartnerGroups(authentication);
    }

    @PostMapping("/partners")
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerGroupResponseDto createPartnerGroup(Authentication authentication, @RequestBody PartnerGroupRequestDto dto) {
        return companyService.createPartnerGroup(authentication, dto);
    }

    @DeleteMapping("/partners")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePartnerGroup(Authentication authentication, @RequestParam("partnerGroupId") Long partnerGroupId) {
        companyService.deletePartnerGroup(authentication, partnerGroupId);
    }

    @PutMapping("/partners/{partnerGroupId}")
    @ResponseStatus(HttpStatus.OK)
    public PartnerGroupResponseDto editPartnerGroup(Authentication authentication,
                                                    @PathVariable Long partnerGroupId,
                                                    @RequestBody PartnerGroupRequestDto dto) {
        return companyService.editPartnerGroup(authentication, partnerGroupId, dto);
    }

    @DeleteMapping("/partners/{partnerGroupId}/companies/{companyId}")
    @ResponseStatus(HttpStatus.OK)
    public PartnerGroupResponseDto removeCompanyFromPartners(Authentication authentication,
                                                             @PathVariable("partnerGroupId") Long partnerGroupId,
                                                             @PathVariable("companyId") Long companyId) {
        return companyService.removeCompanyFromPartners(authentication, partnerGroupId, companyId);
    }

    @GetMapping("/{id}/with-users")
    @ResponseStatus(HttpStatus.OK)
    public CompaniesAndUsersResponseDto getCompanyAndUsers(@PathVariable("id") Long companyId) {
        return companyService.getCompanyAndUsers(companyId);
    }

    @GetMapping("/confirm-email")
    @ResponseStatus(HttpStatus.OK)
    public void confirmCompanyEmail(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        companyService.confirmCompanyEmail(token);
        response.sendRedirect(frontEndAddress + "/company/profile");
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public CompanyResponseDto editCompany(Authentication authentication,
                                          @RequestBody @Valid CompanyRequestDto companyRequestDto,
                                          HttpServletRequest request) {
        return companyService.editCompany(authentication, companyRequestDto, request);
    }

    @PostMapping(value = "/images/set", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public CompanyResponseDto setCompanyImages(Authentication authentication,
                                               @RequestParam(value = "image", required = false) MultipartFile image,
                                               @RequestParam(value = "banner", required = false) MultipartFile banner) {
        return companyService.setCompanyImages(authentication, image, banner);
    }

    @DeleteMapping("/images/banner")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompanyBanner(Authentication authentication) {
        companyService.deleteCompanyBanner(authentication);
    }

    @DeleteMapping("/images/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompanyImage(Authentication authentication) {
        companyService.deleteCompanyImage(authentication);
    }

    @PostMapping("/filters")
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyFilterResponseDto addCompanyFilter(Authentication authentication,
                                                     @RequestBody @Valid CompanyFilterRequestDto dto) {
        return companyService.addCompanyFilter(authentication, dto);
    }

    @GetMapping("/filters/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyFilterResponseDto getCompanyFilter(@PathVariable("id") Long id, Authentication authentication) {
        return companyService.getCompanyFilter(id, authentication);
    }

    @GetMapping("/filters")
    @ResponseStatus(HttpStatus.OK)
    public List<CompanyFilterResponseDto> getCompanyFilters(Authentication authentication) {
        return companyService.getCompanyFilters(authentication);
    }

    @PutMapping("/filters/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyFilterResponseDto editCompanyFilter(@PathVariable("id") Long id,
                                                      @RequestBody @Valid CompanyFilterEditDto dto,
                                                      Authentication authentication) {
        return companyService.editCompanyFilter(id, dto, authentication);
    }

    @DeleteMapping("/filters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompanyFilter(@PathVariable("id") Long id, Authentication authentication) {
        companyService.deleteCompanyFilter(id, authentication);
    }

    @PostMapping("/talents")
    @ResponseStatus(HttpStatus.CREATED)
    public TalentResponseDto createTalent(Authentication authentication,
                                          @RequestBody @Valid TalentRequestDto talentRequestDto) {
        return companyService.createTalent(authentication, talentRequestDto);
    }

    @PutMapping("/talents/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public TalentResponseDto updateTalent(Authentication authentication,
                                          @PathVariable("id") Long talentId,
                                          @RequestBody @Valid TalentRequestDto talentRequestDto) {
        return companyService.updateTalent(authentication, talentId, talentRequestDto);
    }

    @GetMapping("/talents")
    @ResponseStatus(HttpStatus.OK)
    public Page<TalentResponseDto> getAllTalents(Authentication authentication,
                                                 @RequestParam(defaultValue = "0") int offset,
                                                 @RequestParam(defaultValue = "10")int pageSize,
                                                 @RequestParam String sort,
                                                 @RequestParam boolean ascending,
                                                 @RequestParam List<Long> workModesIds,
                                                 @RequestParam List<Long> skillsIds,
                                                 @RequestParam Integer rate) {
        return companyService.getAllTalents(authentication,
                offset,
                pageSize,
                sort,
                ascending,
                workModesIds,
                skillsIds,
                rate);
    }

    @GetMapping("/talents/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TalentResponseDto getById(Authentication authentication, @PathVariable("id") Long id) {
        return companyService.getTalentById(authentication, id);
    }

    @GetMapping("/my-talents")
    @ResponseStatus(HttpStatus.OK)
    public List<TalentResponseDto> getMyTalents(Authentication authentication) {
        return companyService.getMyTalents(authentication);
    }

    @GetMapping("/my-talents/partial")
    @ResponseStatus(HttpStatus.OK)
    public List<PartialTalentResponseDto> getMyTalentsPartial(Authentication authentication){
        return companyService.getMyTalentsPartial(authentication);
    }

    @PutMapping("/my-talents/publicity")
    @ResponseStatus(HttpStatus.OK)
    public void setTalentVisibility(Authentication authentication, @RequestBody TalentPublicityRequestDto talentPublicityRequestDto) {
        companyService.setTalentVisibility(authentication, talentPublicityRequestDto);
    }

    @GetMapping("/my-talents/publicity")
    @ResponseStatus(HttpStatus.OK)
    public TalentPublicityResponseDto getTalentVisibility(Authentication authentication) {
        return companyService.getTalentVisibility(authentication);
    }

    @DeleteMapping("/talents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTalent(Authentication authentication, @PathVariable("id") Long id) {
        companyService.deleteTalent(authentication, id);
    }
    
    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public PositionApplicationResponseDto applyForPosition(
            Authentication authentication,
            @RequestBody PositionApplicationRequestDto requestDto) {
        return positionApplicationService.applyForPosition(authentication, requestDto);
    }

    @PostMapping(value = "/upload-cv", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public PositionApplicationResponseDto uploadCV(@RequestParam("file") MultipartFile file,
                           @RequestParam("application_id") Long applicationId) {
        return positionApplicationService.uploadCV(file, applicationId);
    }


    @GetMapping("/applications")
    @ResponseStatus(HttpStatus.OK)
    public List<PositionApplicationResponseDto> getApplicationsForMyPositions(Authentication authentication) {
        return positionApplicationService.getApplicationsForMyPositions(authentication);
    }

    @GetMapping("/applications/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionApplicationResponseDto getApplicationById(Authentication authentication, @PathVariable("id") Long applicationId) {
        return positionApplicationService.getApplicationById(authentication, applicationId);
    }

    @PutMapping(value = "/applications", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public PositionApplicationResponseDto updateApplication(
            Authentication authentication,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("application_id") Long applicationId,
            @RequestParam(value = "talent_id", required = false) Long talentId) {
        return positionApplicationService.updateApplication(authentication, file, applicationId, talentId);
    }

    @PutMapping("/applications/accept/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionApplicationResponseDto acceptApplication(Authentication authentication, @PathVariable("id") Long applicationId) {
        return positionApplicationService.acceptApplication(authentication, applicationId);
    }

    @PutMapping("/applications/reject/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionApplicationResponseDto rejectApplication(Authentication authentication, @PathVariable("id") Long applicationId) {
        return positionApplicationService.rejectApplication(authentication, applicationId);
    }

    @GetMapping("/my-applications")
    @ResponseStatus(HttpStatus.OK)
    public List<PositionApplicationResponseDto> getMyApplications(Authentication authentication) {
        return positionApplicationService.getMyApplications(authentication);
    }
}
