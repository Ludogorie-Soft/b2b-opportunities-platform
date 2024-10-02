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
import com.example.b2b_opportunities.Exception.PermissionDeniedException;
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
        if (authentication == null)
            throw new AuthenticationFailedException("User not authenticated");
        if (image == null || image.isEmpty())
            throw new NotFoundException("Image is missing or empty");
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getCompany() != null)
            throw new AlreadyExistsException(currentUser.getUsername() + " is already associated with Company: " + currentUser.getCompany().getName());

        validateCompanyRequestInput(companyRequestDto);
        Company company = companyRepository.save(setCompanyFields(companyRequestDto));

        handleCompanyEmailVerification(authentication, company, request);

        addCompanyToUser(authentication, company);

        imageService.upload(image, company.getId(), "image");
        if (banner != null && !banner.isEmpty()) {
            imageService.upload(banner, company.getId(), "banner");
        }
        return generateCompanyResponseDto(company);
    }


    private void handleCompanyEmailVerification(Authentication authentication, Company company, HttpServletRequest request) {
        if (getCurrentUser(authentication).getEmail().equals(company.getEmail())) {
            company.setEmailVerification(EmailVerification.ACCEPTED);
        } else {
            company.setEmailVerification(EmailVerification.PENDING);
            mailService.sendCompanyEmailConfirmation(company, request);
        }
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

    public CompanyResponseDto editCompany(Authentication authentication,
                                          CompanyRequestDto companyRequestDto,
                                          MultipartFile image,
                                          MultipartFile banner) {
        User user = getCurrentUser(authentication);
        Company userCompany = user.getCompany();
        if (userCompany == null) {
            throw new NotFoundException("User " + user.getUsername() + " is not associated with any company.");
        }

        updateCompanyName(userCompany, companyRequestDto);
        updateCompanyEmail(userCompany, companyRequestDto);
        updateCompanyWebsiteAndLinkedIn(userCompany, companyRequestDto);
        updateCompanyImages(userCompany, image, banner);
        updateOtherCompanyFields(userCompany, companyRequestDto);
        Company company = companyRepository.save(userCompany);

        return generateCompanyResponseDto(company);
    }

    private void updateCompanyName(Company userCompany, CompanyRequestDto companyRequestDto) {
        String newName = companyRequestDto.getName().trim().replaceAll("\\s+", " ");
        if (!newName.equals(userCompany.getName())) {
            if (companyRepository.findByNameIgnoreCase(newName).isPresent()) {
                throw new AlreadyExistsException("Name already registered");
            }
            userCompany.setName(newName);
        }
    }

    private void updateCompanyEmail(Company userCompany, CompanyRequestDto companyRequestDto) {
        if (!companyRequestDto.getEmail().equals(userCompany.getEmail())) {
            if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent()) {
                throw new AlreadyExistsException("Email already registered");
            }
            if (userCompany.getEmailVerification().equals(EmailVerification.ACCEPTED)) {
                throw new PermissionDeniedException("Email cannot be changed because it has already been verified.");
            }
            userCompany.setEmail(companyRequestDto.getEmail());
            userCompany.setEmailVerification(EmailVerification.PENDING);
        }
    }

    private void updateCompanyWebsiteAndLinkedIn(Company userCompany, CompanyRequestDto companyRequestDto) {
        String newWebsite = companyRequestDto.getWebsite();
        if (!newWebsite.equals(userCompany.getWebsite())) {
            if (companyRepository.findByWebsite(newWebsite).isPresent()) {
                throw new AlreadyExistsException("Website already registered");
            }
            userCompany.setWebsite(newWebsite);
        }

        String newLinkedIn = companyRequestDto.getLinkedIn();
        if (!newLinkedIn.equals(userCompany.getLinkedIn())) {
            if (companyRepository.findByLinkedIn(newLinkedIn).isPresent()) {
                throw new AlreadyExistsException("LinkedIn already registered");
            }
            userCompany.setLinkedIn(newLinkedIn);
        }
    }

    private void updateCompanyImages(Company userCompany, MultipartFile image, MultipartFile banner) {
        if (image != null && !image.isEmpty()) {
            imageService.upload(image, userCompany.getId(), "image");
        }

        if (banner != null && !banner.isEmpty()) {
            imageService.upload(banner, userCompany.getId(), "banner");
        }
    }

    private void updateOtherCompanyFields(Company userCompany, CompanyRequestDto companyRequestDto) {
        if (!userCompany.getCompanyType().getId().equals(companyRequestDto.getCompanyTypeId())) {
            userCompany.setCompanyType(companyTypeRepository.findById(companyRequestDto.getCompanyTypeId())
                    .orElseThrow(() -> new NotFoundException("Company type not found")));
        }
        if (!userCompany.getDomain().getId().equals(companyRequestDto.getDomainId())) {
            userCompany.setDomain(domainRepository.findById(companyRequestDto.getDomainId())
                    .orElseThrow(() -> new NotFoundException("Domain not found")));
        }
        if (!userCompany.getSkills().equals(skillRepository.findAllByIdIn(companyRequestDto.getSkills()))) {
            patternService.getAllSkillsIfSkillIdsExist(companyRequestDto.getSkills());
            userCompany.setSkills(skillRepository.findAllByIdIn(companyRequestDto.getSkills()));
        }
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

    private Company setCompanyFields(CompanyRequestDto companyRequestDto) {
        Company company = CompanyMapper.toCompany(companyRequestDto);
        company.setCompanyType(companyTypeRepository.findById(companyRequestDto.getCompanyTypeId()).orElseThrow());
        company.setDomain(domainRepository.findById(companyRequestDto.getDomainId()).orElseThrow());
        company.setSkills(skillRepository.findAllByIdIn(companyRequestDto.getSkills()));
        return company;
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private void validateCompanyRequestInput(CompanyRequestDto companyRequestDto) {
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