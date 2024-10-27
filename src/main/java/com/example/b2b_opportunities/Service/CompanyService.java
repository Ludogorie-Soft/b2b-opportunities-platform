package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyFilterEditDto;
import com.example.b2b_opportunities.Dto.Request.CompanyFilterRequestDto;
import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyFilterResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyPublicResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Dto.Response.PartnerGroupResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.Filter;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Mapper.FilterMapper;
import com.example.b2b_opportunities.Mapper.PartnerGroupMapper;
import com.example.b2b_opportunities.Mapper.ProjectMapper;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Repository.DomainRepository;
import com.example.b2b_opportunities.Repository.FilterRepository;
import com.example.b2b_opportunities.Repository.PartnerGroupRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.Utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.b2b_opportunities.Mapper.CompanyMapper.toCompanyPublicResponseDtoList;
import static com.example.b2b_opportunities.Utils.EmailUtils.validateEmail;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final ImageService imageService;
    private final CompanyTypeRepository companyTypeRepository;
    private final DomainRepository domainRepository;
    private final PatternService patternService;
    private final MailService mailService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final FilterRepository filterRepository;
    private final PartnerGroupRepository partnerGroupRepository;

    public CompanyResponseDto createCompany(Authentication authentication,
                                            CompanyRequestDto companyRequestDto,
                                            HttpServletRequest request) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        validateUserIsNotAssociatedWithAnotherCompany(currentUser);

        validateCompanyRequestInput(companyRequestDto);
        validateEmail(companyRequestDto.getEmail());
        Company company = setCompanyFields(companyRequestDto);
        company.getUsers().add(currentUser);
        setCompanyEmailVerificationStatusAndSendEmail(company, currentUser, companyRequestDto, request);

        company = companyRepository.save(company);
        currentUser.setCompany(company);
        userRepository.saveAndFlush(currentUser);

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
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = getUserCompanyOrThrow(currentUser);

        updateCompanyName(userCompany, companyRequestDto);
        validateEmail(companyRequestDto.getEmail());
        if (updateCompanyEmailIfChanged(userCompany, companyRequestDto)) {
            setCompanyEmailVerificationStatusAndSendEmail(userCompany, currentUser, companyRequestDto, request);
        }
        updateCompanyWebsiteAndLinkedIn(userCompany, companyRequestDto);
        updateOtherCompanyFields(userCompany, companyRequestDto);
        Company company = companyRepository.save(userCompany);

        return generateCompanyResponseDto(company);
    }

    public CompanyResponseDto setCompanyImages(Authentication authentication,
                                               MultipartFile image,
                                               MultipartFile banner) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);

        updateCompanyImage(company.getId(), image, "image");
        updateCompanyImage(company.getId(), banner, "banner");

        return generateCompanyResponseDto(company);
    }

    public void deleteCompanyBanner(Authentication authentication) {
        delete(authentication, "banner");
    }

    public void deleteCompanyImage(Authentication authentication) {
        delete(authentication, "image");
    }

    public List<ProjectResponseDto> getCompanyProjects(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company with ID: " + companyId + " not found"));
        return ProjectMapper.toDtoList(company.getProjects());
    }

    public List<CompanyFilterResponseDto> getCompanyFilters(Authentication authentication) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = getUserCompanyOrThrow(currentUser);

        Set<Filter> filters = userCompany.getFilters();
        return FilterMapper.toDtoList(filters);
    }

    public CompanyFilterResponseDto getCompanyFilter(Long id, Authentication authentication) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);
        validateFilterIsRelatedToTheCompany(id, company);

        return FilterMapper.toDto(getFilterIfExists(id));
    }

    public CompanyFilterResponseDto editCompanyFilter(Long id, CompanyFilterEditDto dto, Authentication authentication) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);
        validateFilterIsRelatedToTheCompany(id, company);

        Filter filter = mapToFilter(dto, company);
        filter.setId(id);
        return FilterMapper.toDto(filterRepository.save(filter));
    }

    public void deleteCompanyFilter(Long id, Authentication authentication) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);
        validateFilterIsRelatedToTheCompany(id, company);

        filterRepository.deleteById(id);
    }

    public CompanyFilterResponseDto addCompanyFilter(Authentication authentication, @Valid CompanyFilterRequestDto dto) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = getUserCompanyOrThrow(currentUser);

        Filter filter = mapToFilter(dto, userCompany);

        filter = filterRepository.save(filter);

        userCompany.getFilters().add(filter);
        companyRepository.save(userCompany);

        return FilterMapper.toDto(filter);
    }

    public List<PartnerGroupResponseDto> getPartnerGroups(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(user);
        Set<PartnerGroup> partnerGroups = company.getPartnerGroups();
        return partnerGroups.stream().map(PartnerGroupMapper::toPartnerGroupResponseDto).collect(Collectors.toList());
    }

    public PartnerGroupResponseDto removeCompanyFromPartners(Authentication authentication, Long partnerGroupId, Long companyId) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(user);
        Set<PartnerGroup> partnerGroups = company.getPartnerGroups();
        PartnerGroup partnerGroup = partnerGroups.stream()
                .filter(pg -> pg.getId().equals(partnerGroupId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Partner group with ID: " + partnerGroupId + " not found for this company."));
        Company companyToBeRemoved = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Company with ID: " + companyId + " not found"));
        checkIfCompanyIsInPartnerGroup(partnerGroup, companyToBeRemoved);
        partnerGroup.getPartners().remove(companyToBeRemoved);
        return PartnerGroupMapper.toPartnerGroupResponseDto(partnerGroupRepository.save(partnerGroup));
    }

    public void deletePartnerGroup(Authentication authentication, Long partnerGroupId) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(user);
        Set<PartnerGroup> partnerGroups = company.getPartnerGroups();
        PartnerGroup partnerGroupToBeRemoved = partnerGroupRepository.findById(partnerGroupId)
                .orElseThrow(() -> new NotFoundException("Partner group with ID: " + partnerGroupId + " not found"));
        if (partnerGroups.contains(partnerGroupToBeRemoved)) {
            partnerGroups.remove(partnerGroupToBeRemoved);
            partnerGroupRepository.delete(partnerGroupToBeRemoved);
        } else {
            throw new PermissionDeniedException("This Partner group does not belong to this company");
        }
    }

    public List<CompanyPublicResponseDto> getAcceptedCompaniesPublicData() {
        List<Company> verifiedCompanies = companyRepository.findCompaniesByEmailVerificationAccepted();
        List<CompanyPublicResponseDto> companiesPublicDataNoImg = toCompanyPublicResponseDtoList(verifiedCompanies);

        List<CompanyPublicResponseDto> result = new ArrayList<>();

        for (CompanyPublicResponseDto c : companiesPublicDataNoImg) {
            c.setImage(imageService.returnUrlIfPictureExists(c.getId(), "image"));
            result.add(c);
        }

        return result;
    }

    public PartnerGroupResponseDto createPartnerGroup(Authentication authentication, String partnershipName) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(user); //check if user belongs to a company

        if (company.getPartnerGroups().stream().map(PartnerGroup::getName).toList().contains(partnershipName)) {
            throw new AlreadyExistsException("Partner group: '" + partnershipName + "' already exists.");
        }

        PartnerGroup partnerGroup = partnerGroupRepository.save(PartnerGroup.builder()
                .name(partnershipName)
                .company(company)
                .build());

        company.getPartnerGroups().add(partnerGroup);
        companyRepository.save(company);
        return PartnerGroupMapper.toPartnerGroupResponseDto(partnerGroup);
    }

    public PartnerGroupResponseDto addCompanyToPartners(Authentication authentication, Long partnerGroupId, Long partnerCompanyId) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = getUserCompanyOrThrow(user);
        if (userCompany.getId().equals(partnerCompanyId))
            throw new InvalidRequestException("You can't add your company to a partner group");

        Company potentialPartnerCompany = companyRepository.findById(partnerCompanyId)
                .orElseThrow(() -> new NotFoundException("Company with ID: " + partnerCompanyId + " not found"));
        PartnerGroup partnerGroup = partnerGroupRepository.findById(partnerGroupId)
                .orElseThrow(() -> new NotFoundException("Partner group with ID: " + partnerGroupId + " not found"));
        validatePartnerGroupBelongsToCompany(partnerGroup, userCompany);
        checkIfPartnersAlready(potentialPartnerCompany, partnerGroup);
        partnerGroup.getPartners().add(potentialPartnerCompany);
        partnerGroupRepository.save(partnerGroup);
        return PartnerGroupMapper.toPartnerGroupResponseDto(partnerGroup);
    }

    private Filter getFilterIfExists(Long id) {
        return filterRepository.findById(id).orElseThrow(() -> new NotFoundException("Filter with ID: " + id + " not found."));
    }

    private void validateFilterIsRelatedToTheCompany(Long id, Company company) {
        getFilterIfExists(id);  // Throw an error if ID doesn't exist.
        boolean idExists = company.getFilters().stream()
                .map(Filter::getId)
                .anyMatch(filterId -> filterId.equals(id));

        if (!idExists) {
            throw new NotFoundException("The filter ID: " + id + " is not related to the company.");
        }
    }

    private Filter mapToFilter(CompanyFilterRequestDto dto, Company company) {
        Filter filter = FilterMapper.toEntity(dto);  // set name + IsEnabled
        filter.setSkills(new HashSet<>(patternService.getAllAssignableSkillsIfSkillIdsExist(dto.getSkillIds().stream().toList()))); // TODO: list to set
        filter.setCompany(company);
        return filter;
    }

    private void delete(Authentication authentication, String imageOrBanner) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);
        if (imageService.doesImageExist(company.getId(), imageOrBanner)) {
            imageService.deleteBanner(company.getId());
        } else {
            throw new NotFoundException(StringUtils.stripCapitalizeAndValidateNotEmpty(imageOrBanner, imageOrBanner) +
                    " doesn't exist for company with ID: " + company.getId());
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

    private boolean updateCompanyEmailIfChanged(Company userCompany, CompanyRequestDto companyRequestDto) {
        String newEmail = companyRequestDto.getEmail();
        if (!newEmail.equals(userCompany.getEmail())) {
            if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent()) {
                throw new AlreadyExistsException("Email already registered");
            }
            userCompany.setEmail(companyRequestDto.getEmail());
            return true; //mail was changed
        }
        return false; //mail was not changed
    }

    private void setCompanyEmailVerificationStatusAndSendEmail(Company userCompany, User currentUser, CompanyRequestDto dto, HttpServletRequest request) {
        EmailVerification status = setCompanyEmailVerificationStatus(userCompany, currentUser.getEmail(), dto.getEmail());
        if (status.equals(EmailVerification.PENDING)) {
            String token = UUID.randomUUID().toString();
            userCompany.setEmailConfirmationToken(token);
            companyRepository.save(userCompany);
            mailService.sendCompanyEmailConfirmation(userCompany, token, request);
        }
    }

    private void updateCompanyWebsiteAndLinkedIn(Company userCompany, CompanyRequestDto companyRequestDto) {
        String newWebsite = companyRequestDto.getWebsite();
        if (newWebsite != null && !newWebsite.isEmpty() && !newWebsite.equals(userCompany.getWebsite())) {
            if (companyRepository.findByWebsite(newWebsite).isPresent()) {
                throw new AlreadyExistsException("Website already registered");
            }
            userCompany.setWebsite(newWebsite);
        }

        String newLinkedIn = companyRequestDto.getLinkedIn();
        if (newLinkedIn != null && !newLinkedIn.isEmpty() && !newLinkedIn.equals(userCompany.getLinkedIn())) {
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
        if (dto.getCompanyTypeId() != null && !company.getCompanyType().getId().equals(dto.getCompanyTypeId())) {
            company.setCompanyType(getCompanyTypeOrThrow(dto));
        }
        if (dto.getDomainId() != null && !company.getDomain().getId().equals(dto.getDomainId())) {
            company.setDomain(getDomainOrThrow(dto));
        }
        List<Long> companySkills = company.getSkills().stream().map(Skill::getId).toList();
        if (!companySkills.equals(dto.getSkills())) {
            company.setSkills(getSkillsOrThrow(dto));
        }
        if (dto.getDescription() != null && !dto.getDescription().isEmpty() && !dto.getDescription().equals(company.getDescription())) {
            company.setDescription(dto.getDescription());
        }
    }

    private void validateCompanyRequestInput(CompanyRequestDto companyRequestDto) {
        if (companyRepository.findByNameIgnoreCase(companyRequestDto.getName()).isPresent())
            throw new AlreadyExistsException("Name already registered");
        if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent())
            throw new AlreadyExistsException("Email already registered");

        String website = companyRequestDto.getWebsite();
        if (website != null && !website.isEmpty()) {
            if (companyRepository.findByWebsite(website).isPresent())
                throw new AlreadyExistsException("Website already registered");
        }

        String linkedIn = companyRequestDto.getLinkedIn();
        if (linkedIn != null && !linkedIn.isEmpty()) {
            if (companyRepository.findByLinkedIn(linkedIn).isPresent())
                throw new AlreadyExistsException("LinkedIn already registered");
        }
    }

    private void validatePartnerGroupBelongsToCompany(PartnerGroup partnerGroup, Company company) {
        if (!company.getPartnerGroups().contains(partnerGroup)) {
            throw new PermissionDeniedException("Partner group does not belong to this company");
        }
    }

    private void checkIfPartnersAlready(Company partnerCompany, PartnerGroup partnerGroup) {
        if (partnerGroup.getPartners().contains(partnerCompany)) {
            throw new AlreadyExistsException("This company is already in this partner group");
        }
    }

    private void checkIfCompanyIsInPartnerGroup(PartnerGroup partnerGroup, Company company) {
        if (!partnerGroup.getPartners().contains(company)) {
            throw new InvalidRequestException("Company with ID: " + company.getId() + " is not part of this Partner group");
        }
    }
}