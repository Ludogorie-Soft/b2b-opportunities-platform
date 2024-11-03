package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyFilterEditDto;
import com.example.b2b_opportunities.Dto.Request.CompanyFilterRequestDto;
import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Request.PartnerGroupRequestDto;
import com.example.b2b_opportunities.Dto.Request.SkillExperienceRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentExperienceRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyFilterResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyPublicResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Dto.Response.PartnerGroupResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentResponseDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.Experience;
import com.example.b2b_opportunities.Entity.Filter;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Pattern;
import com.example.b2b_opportunities.Entity.Seniority;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.SkillExperience;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.TalentExperience;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Mapper.FilterMapper;
import com.example.b2b_opportunities.Mapper.PartnerGroupMapper;
import com.example.b2b_opportunities.Mapper.ProjectMapper;
import com.example.b2b_opportunities.Mapper.TalentMapper;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Repository.DomainRepository;
import com.example.b2b_opportunities.Repository.ExperienceRepository;
import com.example.b2b_opportunities.Repository.FilterRepository;
import com.example.b2b_opportunities.Repository.PartnerGroupRepository;
import com.example.b2b_opportunities.Repository.PatternRepository;
import com.example.b2b_opportunities.Repository.SeniorityRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import com.example.b2b_opportunities.Repository.TalentExperienceRepository;
import com.example.b2b_opportunities.Repository.TalentRepository;
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
import java.util.Objects;
import java.util.Optional;
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
    private final SkillRepository skillRepository;
    private final PatternRepository patternRepository;
    private final SeniorityRepository seniorityRepository;
    private final TalentRepository talentRepository;
    private final ExperienceRepository experienceRepository;
    private final TalentExperienceRepository talentExperienceRepository;

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
        PartnerGroup partnerGroupToBeRemoved = getPartnerGroupOrThrow(partnerGroupId);
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

    public PartnerGroupResponseDto createPartnerGroup(Authentication authentication, PartnerGroupRequestDto dto) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(user); //check if user belongs to a company

        if (company.getPartnerGroups().stream().map(PartnerGroup::getName).toList().contains(dto.getName())) {
            throw new AlreadyExistsException("Partner group: '" + dto.getName() + "' already exists.");
        }

        Set<Company> companies = fetchCompaniesByIds(dto.getCompanyIds());
        validateUserCompanyNotInPartnerGroup(dto, company);

        PartnerGroup partnerGroup = partnerGroupRepository.save(PartnerGroup.builder()
                .name(dto.getName())
                .company(company)
                .partners(companies)
                .build());

        company.getPartnerGroups().add(partnerGroup);
        companyRepository.save(company);
        return PartnerGroupMapper.toPartnerGroupResponseDto(partnerGroup);
    }

    public PartnerGroupResponseDto editPartnerGroup(Authentication authentication, Long partnerGroupId, PartnerGroupRequestDto dto) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = getUserCompanyOrThrow(user);
        PartnerGroup partnerGroup = getPartnerGroupOrThrow(partnerGroupId);
        validatePartnerGroupBelongsToUserCompany(userCompany, partnerGroup);

        Set<Company> partners = fetchCompaniesByIds(dto.getCompanyIds());
        validateUserCompanyNotInPartnerGroup(dto, userCompany);

        partnerGroup.setName(dto.getName());
        partnerGroup.setPartners(partners);

        partnerGroupRepository.save(partnerGroup);
        return PartnerGroupMapper.toPartnerGroupResponseDto(partnerGroup);
    }

    public TalentResponseDto createTalent(Authentication authentication, TalentRequestDto talentRequestDto) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        Talent talent = TalentMapper.toEntity(talentRequestDto);
        talent.setCompany(company);
        validateSkills(talentRequestDto);
        talentRepository.save(talent);

        setTalentExperience(talentRequestDto.getTalentExperienceRequestDto(), talent);
        return TalentMapper.toResponseDto(talent);
    }

    public TalentResponseDto updateTalent(Authentication authentication, Long talentId, TalentRequestDto talentRequestDto) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        Talent talent = getTalentOrThrow(talentId);
        validateTalentBelongsToCompany(company, talent);
        validateSkills(talentRequestDto);

        talent.getExperienceList().clear();
        talentRepository.save(talent);
        talentExperienceRepository.deleteAllByTalentId(talentId);

        setTalentExperience(talentRequestDto.getTalentExperienceRequestDto(), talent);
        updateTalentStatusAndInfo(talentRequestDto, talent);
        return TalentMapper.toResponseDto(talentRepository.save(talent));
    }

    public List<TalentResponseDto> getAllTalents(Authentication authentication) {
        //TODO - do we want only logged-in users to have access to the talents?
        userService.getCurrentUserOrThrow(authentication);
        List<Talent> talents = talentRepository.findAll();
        return talents.stream().map(TalentMapper::toResponseDto).toList();
    }

    public TalentResponseDto getTalentById(Long talentId) {
        return TalentMapper.toResponseDto(talentRepository.findById(talentId)
                .orElseThrow(() -> new NotFoundException("Talent with ID: " + talentId + " not found")));
    }

    public List<TalentResponseDto> getMyTalents(Authentication authentication) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        List<Talent> myTalents = talentRepository.findByCompanyId(company.getId());
        return myTalents.stream().map(TalentMapper::toResponseDto).toList();
    }

    public void deleteTalent(Authentication authentication, Long id) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        Talent talent = getTalentOrThrow(id);
        validateTalentBelongsToCompany(company, talent);
        talentRepository.delete(talent);
    }

    private void updateTalentStatusAndInfo(TalentRequestDto dto, Talent talent) {
        talent.setDescription(dto.getDescription());
        talent.setResidence(dto.getResidence());
        talent.setActive(dto.isActive());
    }

    private void validateTalentBelongsToCompany(Company company, Talent talent) {
        if (!Objects.equals(talent.getCompany().getId(), company.getId())) {
            throw new PermissionDeniedException("This talent does not belong to your company");
        }
    }

    private void validatePartnerGroupBelongsToUserCompany(Company company, PartnerGroup partnerGroup) {
        if (partnerGroup.getCompany() != company) {
            throw new PermissionDeniedException("This partner group does not belong to this company");
        }
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
        filter.setSkills(new HashSet<>(patternService.getAllAssignableSkillsIfSkillIdsExist(dto.getSkills().stream().toList()))); // TODO: list to set
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

    private void checkIfCompanyIsInPartnerGroup(PartnerGroup partnerGroup, Company company) {
        if (!partnerGroup.getPartners().contains(company)) {
            throw new InvalidRequestException("Company with ID: " + company.getId() + " is not part of this Partner group");
        }
    }

    private PartnerGroup getPartnerGroupOrThrow(Long partnerGroupId) {
        return partnerGroupRepository.findById(partnerGroupId)
                .orElseThrow(() -> new NotFoundException("Partner group with ID: " + partnerGroupId + " not found"));
    }

    private void validateUserCompanyNotInPartnerGroup(PartnerGroupRequestDto dto, Company userCompany) {
        if (dto.getCompanyIds().contains(userCompany.getId())) {
            throw new InvalidRequestException("You can't add your company to a partner group");
        }
    }

    private Set<Company> fetchCompaniesByIds(Set<Long> companyIds) {
        return companyIds.stream().map(id -> companyRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Company with ID: " + id + " not found")))
                .collect(Collectors.toSet());
    }

    private void setTalentExperience(TalentExperienceRequestDto dto, Talent talent) {
        TalentExperience talentExperience = createTalentExperience(dto, talent);
        List<SkillExperience> skillExperienceList = processSkillExperiences(dto.getSkillExperienceRequestDtoList(), talentExperience);
        talentExperience.setSkillExperienceList(skillExperienceList);
        talent.getExperienceList().add(talentExperience);
        talentExperienceRepository.save(talentExperience);
    }

    private TalentExperience createTalentExperience(TalentExperienceRequestDto dto, Talent talent) {
        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setTalent(talent);
        talentExperience.setPattern(getPatternOrThrow(dto.getPatternId()));
        talentExperience.setSeniority(getSeniorityOrThrow(dto.getSeniorityId()));
        return talentExperience;
    }

    private List<SkillExperience> processSkillExperiences(List<SkillExperienceRequestDto> skillExperienceRequests, TalentExperience talentExperience) {
        List<SkillExperience> skillExperienceList = new ArrayList<>();
        int totalTime = 0;

        for (SkillExperienceRequestDto dtoItem : skillExperienceRequests) {
            Skill skill = getSkillOrThrow(dtoItem.getSkillId());
            if (isSkillIsAlreadyInList(skillExperienceList, skill)) {
                continue;
            }
            Experience experience = createAndSaveExperience(dtoItem);
            totalTime += calculateExperienceTime(experience);
            skillExperienceList.add(createSkillExperience(talentExperience, skill, experience));
        }

        talentExperience.setTotalTime(totalTime);
        return skillExperienceList;
    }

    private boolean isSkillIsAlreadyInList(List<SkillExperience> skillExperienceList, Skill skill) {
        for (SkillExperience se : skillExperienceList) {
            if (se.getSkill().getId().equals(skill.getId())) {
                return true;
            }
        }
        return false;
    }

    private void validateSkills(TalentRequestDto talentRequestDto) {
        List<SkillExperienceRequestDto> skillExperienceList = talentRequestDto.getTalentExperienceRequestDto().getSkillExperienceRequestDtoList();
        List<Long> skillIds = skillExperienceList.stream().map(SkillExperienceRequestDto::getSkillId).toList();
        List<Long> nonAssignableSkillIds = skillIds.stream()
                .map(skillRepository::findById)
                .map(Optional::orElseThrow)
                .filter(skill -> !skill.getAssignable())
                .map(Skill::getId)
                .toList();
        if (!nonAssignableSkillIds.isEmpty()) {
            throw new InvalidRequestException("The following skills are not assignable: " + nonAssignableSkillIds);
        }
    }

    private Experience createAndSaveExperience(SkillExperienceRequestDto dtoItem) {
        Experience experience = createExperience(dtoItem.getMonths(), dtoItem.getYears());
        return experienceRepository.save(experience);
    }

    private SkillExperience createSkillExperience(TalentExperience talentExperience, Skill skill, Experience experience) {
        return SkillExperience.builder()
                .talentExperience(talentExperience)
                .skill(skill)
                .experience(experience)
                .build();
    }

    private int calculateExperienceTime(Experience experience) {
        int totalTime = 0;
        totalTime += experience.getMonths() != null ? experience.getMonths() : 0;
        totalTime += experience.getYears() != null ? experience.getYears() * 12 : 0;
        return totalTime;
    }

    private Experience createExperience(Integer months, Integer years) {
        if (months == null && years == null || months != null && months == 0 && years != null && years == 0 || months == null && years == 0 || months != null && months == 0 && years == null) {
            throw new InvalidRequestException("At least one of 'months' or 'years' must be non-zero and non-null.");
        }
        return Experience.builder()
                .years(years)
                .months(months)
                .build();
    }

    private Skill getSkillOrThrow(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill with ID: " + skillId + " not found"));
    }

    private Pattern getPatternOrThrow(Long patternId) {
        return patternRepository.findById(patternId)
                .orElseThrow(() -> new NotFoundException("Pattern with ID: " + patternId + " not found"));
    }

    private Seniority getSeniorityOrThrow(Long seniorityId) {
        return seniorityRepository.findById(seniorityId)
                .orElseThrow(() -> new NotFoundException("Seniority with ID: " + seniorityId + " not found"));
    }

    private Talent getTalentOrThrow(Long talentId) {
        return talentRepository.findById(talentId)
                .orElseThrow(() -> new NotFoundException("Talent with ID: " + talentId + " not found"));
    }

}