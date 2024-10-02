package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Repository.DomainRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final CompanyTypeRepository companyTypeRepository;
    private final DomainRepository domainRepository;
    private final SkillRepository skillRepository;
    private final PatternService patternService;
    private final MailService mailService;

    public CompanyResponseDto createCompany(Authentication authentication,
                                            CompanyRequestDto companyRequestDto,
                                            MultipartFile image,
                                            MultipartFile banner,
                                            HttpServletRequest request) {
        companyRequestDto.setName(companyRequestDto.getName().trim().replaceAll("\\s+", " "));
        validateCompanyCreationInput(companyRequestDto, authentication, image);
        Company company = companyRepository.save(setCompanyFields(companyRequestDto, authentication, request));
        addCompanyToUser(authentication, company);

        imageService.upload(image, company.getId(), "image");
        if (banner != null && !banner.isEmpty()) {
            imageService.upload(banner, company.getId(), "banner");
        }
        return generateCompanyResponseDto(company);
    }

    public CompaniesAndUsersResponseDto getCompaniesAndUsers(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Company with ID: " + companyId + " not found"));
        List<UserResponseDto> users = UserMapper.toResponseDtoList(company.getUsers());
        CompanyResponseDto responseDto = generateCompanyResponseDto(company);

        CompaniesAndUsersResponseDto response = new CompaniesAndUsersResponseDto();
        response.setUsers(users);
        response.setCompany(responseDto);

        return response;
    }

    public String confirmCompanyEmail(String token) {
        Company company = companyRepository.findByEmailConfirmationToken(token).orElseThrow(() -> new NotFoundException("Invalid or already used token"));
        company.setEmailVerification(EmailVerification.ACCEPTED);
        company.setEmailConfirmationToken(null);
        companyRepository.save(company);
        return "Company email verified successfully";
    }

    private void addCompanyToUser(Authentication authentication, Company company) {
        User user = getCurrentUser(authentication);
        user.setCompany(company);
        userRepository.save(user);
    }

    private CompanyResponseDto generateCompanyResponseDto(Company company) {
        CompanyResponseDto companyResponseDto = CompanyMapper.toCompanyResponseDto(company);
        companyResponseDto.setImage(imageService.returnUrlIfPictureExists(company.getId(), "image"));
        companyResponseDto.setBanner(imageService.returnUrlIfPictureExists(company.getId(), "banner"));
        return companyResponseDto;
    }

    private Company setCompanyFields(CompanyRequestDto companyRequestDto, Authentication authentication, HttpServletRequest request) {
        Company company = CompanyMapper.toCompany(companyRequestDto);
        company.setCompanyType(companyTypeRepository.findById(companyRequestDto.getCompanyTypeId()).orElseThrow());
        company.setDomain(domainRepository.findById(companyRequestDto.getDomainId()).orElseThrow());
        company.setSkills(skillRepository.findAllByIdIn(companyRequestDto.getSkills()));
        if (getCurrentUser(authentication).getEmail().equals(company.getEmail())) {
            company.setEmailVerification(EmailVerification.ACCEPTED);
        } else {
            company.setEmailVerification(EmailVerification.PENDING);
            mailService.sendCompanyEmailConfirmation(company, request);
        }
        return company;
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private void validateCompanyCreationInput(CompanyRequestDto companyRequestDto, Authentication authentication, MultipartFile image) {
        if (authentication == null) throw new AuthenticationFailedException("User not authenticated");
        if (image == null || image.isEmpty()) throw new NotFoundException("Image is missing or empty");
        companyTypeRepository.findById(companyRequestDto.getCompanyTypeId()).orElseThrow(() -> new NotFoundException("Company type not found"));
        domainRepository.findById(companyRequestDto.getDomainId()).orElseThrow(() -> new NotFoundException("Domain not found"));
        if (companyRepository.findByNameIgnoreCase(companyRequestDto.getName()).isPresent())
            throw new AlreadyExistsException("Name already registered");
        if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent())
            throw new AlreadyExistsException("Email already registered");
        if (companyRepository.findByWebsite(companyRequestDto.getWebsite()).isPresent())
            throw new AlreadyExistsException("Website already registered");
        if (companyRepository.findByLinkedIn(companyRequestDto.getLinkedIn()).isPresent())
            throw new AlreadyExistsException("LinkedIn already registered");
        patternService.getAllSkillsIfSkillIdsExist(companyRequestDto.getSkills());
    }
}