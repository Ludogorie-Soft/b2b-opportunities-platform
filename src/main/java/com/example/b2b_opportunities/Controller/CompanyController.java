package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyPublicResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Service.CompanyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyRepository companyRepository;
    private final CompanyService companyService;

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
        return CompanyMapper.toCompanyResponseDto(companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company with ID: " + id + " not found")));
    }

    @GetMapping("{id}/projects")
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectResponseDto> getCompanyProjects(@PathVariable("id") Long id) {
        return companyService.getCompanyProjects(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponseDto createCompany(Authentication authentication,
                                            @RequestBody @Valid CompanyRequestDto companyRequestDto,
                                            HttpServletRequest request) {
        return companyService.createCompany(authentication, companyRequestDto, request);
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
}
