package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Repository.DomainRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final ImageService imageService;
    private final CompanyTypeRepository companyTypeRepository;
    private final DomainRepository domainRepository;
    private final PatternService patternService;
    private final MailService mailService;
    private final AdminService adminService;
    private final UserRepository userRepository;

    public CompanyResponseDto createCompany(Authentication authentication,
                                            CompanyRequestDto companyRequestDto,
                                            HttpServletRequest request) {
        User currentUser = adminService.getCurrentUserOrThrow(authentication);
        validateUserIsNotAssociatedWithAnotherCompany(currentUser);

        validateCompanyRequestInput(companyRequestDto);
        Company company = setCompanyFields(companyRequestDto);
        company.setUsers(List.of(currentUser));

        currentUser.setCompany(company);
        userRepository.saveAndFlush(currentUser);
        company = companyRepository.save(company);

        setCompanyEmailVerificationStatusAndSendEmail(company, currentUser, companyRequestDto, request);

        return generateCompanyResponseDto(company);
    }

    public CompaniesAndUsersResponseDto getCompanyAndUsers(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Company with ID: " + companyId + " not found"));
        List<UserResponseDto> users = UserMapper.toResponseDtoList(company.getUsers());
        CompanyResponseDto responseDto = generateCompanyResponseDto(company);

        CompaniesAndUsersResponseDto response = new CompaniesAndUsersResponseDto();
        response.setUsers(users);
        response.setCompany(responseDto);

        return response;
    }

    public void confirmCompanyEmail(String token) {
        Company company = companyRepository.findByEmailConfirmationToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid or already used token"));
        company.setEmailVerification(EmailVerification.ACCEPTED);
        company.setEmailConfirmationToken(null);
        companyRepository.save(company);
    }

    public CompanyResponseDto editCompany(Authentication authentication,
                                          CompanyRequestDto companyRequestDto,
                                          HttpServletRequest request) {
        User currentUser = adminService.getCurrentUserOrThrow(authentication);
        Company userCompany = getUserCompanyOrThrow(currentUser);

        updateCompanyName(userCompany, companyRequestDto);

        updateCompanyEmail(userCompany, companyRequestDto);
        setCompanyEmailVerificationStatusAndSendEmail(userCompany, currentUser, companyRequestDto, request);

        updateCompanyWebsiteAndLinkedIn(userCompany, companyRequestDto);
        updateOtherCompanyFields(userCompany, companyRequestDto);
        Company company = companyRepository.save(userCompany);

        return generateCompanyResponseDto(company);
    }

    public CompanyResponseDto setCompanyImages(Authentication authentication,
                                               MultipartFile image,
                                               MultipartFile banner) {
        User currentUser = adminService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);

        updateCompanyImage(company.getId(), image, "image");
        updateCompanyImage(company.getId(), banner, "banner");

        return generateCompanyResponseDto(company);
    }

    public void deleteCompanyBanner(Authentication authentication) {
        User currentUser = adminService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);
        if (imageService.doesImageExist(company.getId(), "banner")) {
            imageService.deleteBanner(company.getId());
        } else {
            throw new NotFoundException("Banner doesn't exist for company with ID: " + company.getId());
        }
    }

    private void validateUserIsNotAssociatedWithAnotherCompany(User user) {
        if (user.getCompany() != null) {
            throw new AlreadyExistsException(user.getUsername() + " is already associated with Company: " + user.getCompany().getName());
        }
    }

    private EmailVerification setCompanyEmailVerificationStatus(Company userCompany, String userEmail, String newEmail) {
        EmailVerification status = EmailVerification.PENDING;
        if (userEmail.equals(newEmail)) {
            status = EmailVerification.ACCEPTED;
        }
        userCompany.setEmailVerification(status);
        return status;
    }

    private Company getUserCompanyOrThrow(User user) {
        Company userCompany = user.getCompany();
        if (userCompany == null) {
            throw new NotFoundException("User " + user.getUsername() + " is not associated with any company.");
        }
        return userCompany;
    }

    private CompanyResponseDto generateCompanyResponseDto(Company company) {
        CompanyResponseDto companyResponseDto = CompanyMapper.toCompanyResponseDto(company);
        companyResponseDto.setImage(imageService.returnUrlIfPictureExists(company.getId(), "image"));
        companyResponseDto.setBanner(imageService.returnUrlIfPictureExists(company.getId(), "banner"));
        return companyResponseDto;
    }

    private Company setCompanyFields(CompanyRequestDto dto) {
        Company company = CompanyMapper.toCompany(dto);
        company.setCompanyType(getCompanyTypeOrThrow(dto));
        company.setDomain(getDomainOrThrow(dto));
        company.setSkills(getSkillsOrThrow(dto));
        return company;
    }

    private CompanyType getCompanyTypeOrThrow(CompanyRequestDto dto) {
        return companyTypeRepository.findById(dto.getCompanyTypeId())
                .orElseThrow(() -> new NotFoundException("Company type not found"));
    }

    private Domain getDomainOrThrow(CompanyRequestDto dto) {
        return domainRepository.findById(dto.getDomainId())
                .orElseThrow(() -> new NotFoundException("Domain not found"));
    }

    private Set<Skill> getSkillsOrThrow(CompanyRequestDto dto) {
        List<Skill> skills = patternService.getAllSkillsIfSkillIdsExist(dto.getSkills()); // Throws
        // TODO - change LIST to SET
        return new HashSet<>(skills);
    }

    private void updateCompanyName(Company userCompany, CompanyRequestDto companyRequestDto) {
        String newName = companyRequestDto.getName();
        if (!newName.equals(userCompany.getName())) {
            if (companyRepository.findByNameIgnoreCase(newName).isPresent()) {
                throw new AlreadyExistsException("Company name '" + newName + "' already registered");
            }
            userCompany.setName(newName);
        }
    }

    private void updateCompanyEmail(Company userCompany, CompanyRequestDto companyRequestDto) {
        String newEmail = companyRequestDto.getEmail();
        if (!newEmail.equals(userCompany.getEmail())) {
            if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent()) {
                throw new AlreadyExistsException("Email already registered");
            }

            userCompany.setEmail(companyRequestDto.getEmail());
        }
    }

    private void setCompanyEmailVerificationStatusAndSendEmail(Company userCompany, User currentUser, CompanyRequestDto dto, HttpServletRequest request) {
        EmailVerification status = setCompanyEmailVerificationStatus(userCompany, currentUser.getEmail(), dto.getEmail());
        sendEmailIfPending(status, userCompany, request);
    }

    private void sendEmailIfPending(EmailVerification status, Company userCompany, HttpServletRequest request) {
        if (status.equals(EmailVerification.PENDING)) {
            mailService.sendCompanyEmailConfirmation(userCompany, request);
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

    private void updateCompanyImage(Long companyId, MultipartFile multipartFile, String imageName) {
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageService.upload(multipartFile, companyId, imageName);
        }
    }

    private void updateOtherCompanyFields(Company company, CompanyRequestDto dto) {
        if (!company.getCompanyType().getId().equals(dto.getCompanyTypeId())) {
            company.setCompanyType(getCompanyTypeOrThrow(dto));
        }
        if (!company.getDomain().getId().equals(dto.getDomainId())) {
            company.setDomain(getDomainOrThrow(dto));
        }
        List<Long> companySkills = company.getSkills().stream().map(Skill::getId).toList();
        if (!companySkills.equals(dto.getSkills())) {
            company.setSkills(getSkillsOrThrow(dto));
        }
    }

    private void validateCompanyRequestInput(CompanyRequestDto companyRequestDto) {
        if (companyRepository.findByNameIgnoreCase(companyRequestDto.getName()).isPresent())
            throw new AlreadyExistsException("Name already registered");
        if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent())
            throw new AlreadyExistsException("Email already registered");
        if (companyRepository.findByWebsite(companyRequestDto.getWebsite()).isPresent())
            throw new AlreadyExistsException("Website already registered");
        if (companyRepository.findByLinkedIn(companyRequestDto.getLinkedIn()).isPresent())
            throw new AlreadyExistsException("LinkedIn already registered");
    }
}