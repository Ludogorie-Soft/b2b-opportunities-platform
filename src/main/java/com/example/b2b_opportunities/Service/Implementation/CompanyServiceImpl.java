package com.example.b2b_opportunities.Service.Implementation;

import com.example.b2b_opportunities.Dto.Request.CompanyFilterEditDto;
import com.example.b2b_opportunities.Dto.Request.CompanyFilterRequestDto;
import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Request.PartnerGroupRequestDto;
import com.example.b2b_opportunities.Dto.Request.SkillExperienceRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentExperienceRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentPublicityRequestDto;
import com.example.b2b_opportunities.Dto.Request.TalentRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyFilterResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyPublicResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Dto.Response.PartialTalentResponseDto;
import com.example.b2b_opportunities.Dto.Response.PartnerGroupResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentPublicityResponseDto;
import com.example.b2b_opportunities.Dto.Response.TalentResponseDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.Filter;
import com.example.b2b_opportunities.Entity.Location;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Pattern;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.Seniority;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.SkillExperience;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.TalentExperience;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Entity.WorkMode;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.InvalidRequestException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Exception.common.PermissionDeniedException;
import com.example.b2b_opportunities.Mapper.*;
import com.example.b2b_opportunities.Repository.*;
import com.example.b2b_opportunities.Service.Interface.CompanyService;
import com.example.b2b_opportunities.Service.Interface.MailService;
import com.example.b2b_opportunities.Service.Interface.PatternService;
import com.example.b2b_opportunities.Service.Interface.UserService;
import com.example.b2b_opportunities.Static.ApplicationStatus;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.Static.ProjectStatus;
import com.example.b2b_opportunities.Utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.b2b_opportunities.Mapper.CompanyMapper.toCompanyPublicResponseDtoList;
import static com.example.b2b_opportunities.Utils.EmailUtils.validateEmail;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {
    private final PositionApplicationRepository positionApplicationRepository;
    private final CompanyRepository companyRepository;
    private final ImageServiceImpl imageService;
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
    private final TalentExperienceRepository talentExperienceRepository;
    private final SkillExperienceRepository skillExperienceRepository;
    private final LocationRepository locationRepository;
    private final WorkModeRepository workModeRepository;
    private final ProjectRepository projectRepository;

    @Override
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
        createDefaultFilterIfCompanyHasNoSkills(company);
        currentUser.setCompany(company);
        userRepository.saveAndFlush(currentUser);
        log.info("User {} (ID: {}) created company {} (ID: {})", currentUser.getEmail(), currentUser.getId(), company.getName(), company.getId());
        return generateCompanyResponseDto(company);
    }

    @Override
    public CompanyResponseDto getCompany(Long companyId) {
        Company company = getCompanyOrThrow(companyId);
        return generateCompanyResponseDto(company);
    }

    @Override
    public CompaniesAndUsersResponseDto getCompanyAndUsers(Long companyId) {
        Company company = getCompanyOrThrow(companyId);
        List<UserResponseDto> users = UserMapper.toResponseDtoList(company.getUsers());
        CompanyResponseDto responseDto = generateCompanyResponseDto(company);

        CompaniesAndUsersResponseDto response = new CompaniesAndUsersResponseDto();
        response.setUsers(users);
        response.setCompany(responseDto);

        return response;
    }

    @Override
    public void confirmCompanyEmail(String token) {
        Company company = companyRepository.findByEmailConfirmationToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid or already used token"));
        company.setEmailVerification(EmailVerification.ACCEPTED);
        company.setEmailConfirmationToken(null);
        companyRepository.save(company);
        log.info("Confirmed email address for company: {} (ID: {}) ", company.getName(), company.getId());
    }

    @Override
    public CompanyResponseDto editCompany(Authentication authentication,
                                          CompanyRequestDto companyRequestDto,
                                          HttpServletRequest request) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = getUserCompanyOrThrow(currentUser);
        log.info("User (ID: {}) attempting to edit company (ID: {})", currentUser.getId(), userCompany.getId());

        updateCompanyName(userCompany, companyRequestDto);
        validateEmail(companyRequestDto.getEmail());
        if (updateCompanyEmailIfChanged(userCompany, companyRequestDto)) {
            setCompanyEmailVerificationStatusAndSendEmail(userCompany, currentUser, companyRequestDto, request);
        }
        updateCompanyWebsiteAndLinkedIn(userCompany, companyRequestDto);
        updateOtherCompanyFields(userCompany, companyRequestDto);
        Company company = companyRepository.save(userCompany);
        log.info("Done editing company (ID: {})", userCompany.getId());
        return generateCompanyResponseDto(company);
    }

    // TODO - add more logs for the methods below.

    @Override
    public CompanyResponseDto setCompanyImages(Authentication authentication,
                                               MultipartFile image,
                                               MultipartFile banner) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);

        updateCompanyImage(company.getId(), image, "image");
        updateCompanyImage(company.getId(), banner, "banner");

        return generateCompanyResponseDto(company);
    }

    @Override
    public void deleteCompanyBanner(Authentication authentication) {
        delete(authentication, "banner");
    }

    @Override
    public void deleteCompanyImage(Authentication authentication) {
        delete(authentication, "image");
    }

    @Override
    public Set<ProjectResponseDto> getCompanyProjects(Authentication authentication, Long companyId) {
        Company company = getCompanyOrThrow(companyId);
        Company userCompany = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        if (userCompany.getId().equals(companyId)) {
            //if logged user is checking his projects - return all of them
            return ProjectMapper.toDtoSet(new HashSet<>(company.getProjects()));
        }
        if (!company.isApproved()) {
            return new HashSet<>();
        }
        //show public active projects
        List<Project> activeAndNonPartnerOnlyProjects = projectRepository
                .findActiveNonPartnerOnlyProjectsByCompanyId(ProjectStatus.ACTIVE, companyId);

        //show partner only active projects
        List<Project> activePartnerOnlyVisibleToCurrentUserCompanyProjects = projectRepository.
                findActivePartnerOnlyProjectsSharedWithCompany(ProjectStatus.ACTIVE, company.getId(), userCompany.getId());

        Set<Project> projectSet = new HashSet<>();
        projectSet.addAll(activeAndNonPartnerOnlyProjects);
        projectSet.addAll(activePartnerOnlyVisibleToCurrentUserCompanyProjects);

        return ProjectMapper.toDtoSet(projectSet);
    }

    @Override
    public List<CompanyFilterResponseDto> getCompanyFilters(Authentication authentication) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = getUserCompanyOrThrow(currentUser);

        Set<Filter> filters = userCompany.getFilters();
        return FilterMapper.toDtoList(filters);
    }

    @Override
    public CompanyFilterResponseDto getCompanyFilter(Long id, Authentication authentication) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);
        validateFilterIsRelatedToTheCompany(id, company);

        return FilterMapper.toDto(getFilterIfExists(id));
    }

    @Override
    public CompanyFilterResponseDto editCompanyFilter(Long id, CompanyFilterEditDto dto, Authentication authentication) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);
        validateFilterIsRelatedToTheCompany(id, company);

        Filter filter = mapToFilter(dto, company);
        filter.setId(id);
        if (!filter.getSkills().isEmpty()) {
            // Only if the filter has at least one skill.
            disableDefaultFilterIfExistsAndHasNoSkills(company);
        }
        return FilterMapper.toDto(filterRepository.save(filter));
    }

    @Override
    public void deleteCompanyFilter(Long id, Authentication authentication) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(currentUser);
        validateFilterIsRelatedToTheCompany(id, company);

        filterRepository.deleteById(id);
    }

    @Override
    public CompanyFilterResponseDto addCompanyFilter(Authentication authentication, @Valid CompanyFilterRequestDto dto) {
        User currentUser = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = getUserCompanyOrThrow(currentUser);

        Filter filter = mapToFilter(dto, userCompany);

        filter = filterRepository.save(filter);

        userCompany.getFilters().add(filter);
        companyRepository.save(userCompany);

        if (!filter.getSkills().isEmpty()) {
            // Only if the filter has at least one skill.
            disableDefaultFilterIfExistsAndHasNoSkills(userCompany);
        }

        return FilterMapper.toDto(filter);
    }

    @Override
    public List<PartnerGroupResponseDto> getPartnerGroups(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(user);
        Set<PartnerGroup> partnerGroups = company.getPartnerGroups();
        return partnerGroups.stream().map(PartnerGroupMapper::toPartnerGroupResponseDto).collect(Collectors.toList());
    }

    @Override
    public PartnerGroupResponseDto removeCompanyFromPartners(Authentication authentication, Long partnerGroupId, Long companyId) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(user);
        Set<PartnerGroup> partnerGroups = company.getPartnerGroups();
        PartnerGroup partnerGroup = partnerGroups.stream()
                .filter(pg -> pg.getId().equals(partnerGroupId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Partner group with ID: " + partnerGroupId + " not found for this company."));
        Company companyToBeRemoved = getCompanyOrThrow(companyId);
        checkIfCompanyIsInPartnerGroup(partnerGroup, companyToBeRemoved);
        partnerGroup.getPartners().remove(companyToBeRemoved);
        return PartnerGroupMapper.toPartnerGroupResponseDto(partnerGroupRepository.save(partnerGroup));
    }

    @Override
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

    @Override
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

    @Override
    public PartnerGroupResponseDto createPartnerGroup(Authentication authentication, PartnerGroupRequestDto dto) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company company = getUserCompanyOrThrow(user); //check if user belongs to a company

        if (company.getPartnerGroups().stream().map(PartnerGroup::getName).toList().contains(dto.getName())) {
            throw new AlreadyExistsException("Partner group: '" + dto.getName() + "' already exists.", "name");
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

    @Override
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

    @Override
    public TalentResponseDto createTalent(Authentication authentication, TalentRequestDto talentRequestDto) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        Talent talent = TalentMapper.toEntity(talentRequestDto);
        talent.setCompany(company);
        setTalentLocations(talent, talentRequestDto.getLocations());
        setTalentWorkModes(talent, talentRequestDto.getWorkModes());
        setTalentRates(talent, talentRequestDto);
        validateSkills(talentRequestDto);
        talentRepository.save(talent);

        setTalentExperience(talentRequestDto.getExperience(), talent);
        return TalentMapper.toResponseDto(talent);
    }

    @Override
    public TalentResponseDto updateTalent(Authentication authentication, Long talentId, TalentRequestDto
            talentRequestDto) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        Talent talent = getTalentOrThrow(talentId);
        validateTalentBelongsToCompany(company, talent);
        validateSkills(talentRequestDto);
        setTalentRates(talent, talentRequestDto);
        TalentExperience existingTalentExperience = talent.getTalentExperience();
        List<SkillExperience> existingSkills = existingTalentExperience != null
                ? existingTalentExperience.getSkillExperienceList()
                : List.of();

        if (hasSkillsChanged(existingSkills, talentRequestDto.getExperience().getSkills())) {
            talent.setTalentExperience(null);
            talentRepository.save(talent);

            skillExperienceRepository.deleteAll(existingSkills);

            if (existingTalentExperience != null) {
                talentExperienceRepository.delete(existingTalentExperience);
            }
            setTalentExperience(talentRequestDto.getExperience(), talent);
        }

        updateTalentStatusAndInfo(talentRequestDto, talent);
        return TalentMapper.toResponseDto(talentRepository.save(talent));
    }

    @Override
    public Page<TalentResponseDto> getAllTalents(Authentication authentication,
                                                 int offset,
                                                 int pageSize,
                                                 String sort,
                                                 Boolean ascending,
                                                 List<Long> workModesIds,
                                                 List<Long> skillsIds,
                                                 Integer rate) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));

        if (pageSize <= 0) {
            pageSize = 5;
        }

        Sort.Direction direction = (ascending != null && !ascending) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable;
        switch (sort) {
            case "minRate":
                pageable = PageRequest.of(offset, pageSize, Sort.by(direction, "minRate")
                        .and(Sort.by(direction, "maxRate")));
                break;
            case "maxRate":
                pageable = PageRequest.of(offset, pageSize, Sort.by(direction, "maxRate"));
                break;
            case "experience":
                pageable = PageRequest.of(offset, pageSize, Sort.by(direction, "talentExperience.totalTime"));
                break;
            default:
                pageable = PageRequest.of(offset, pageSize);
                break;

        }

        List<Long> workModesFilter = (workModesIds != null && !workModesIds.isEmpty()) ? workModesIds : null;
        List<Long> skillsFilter = (skillsIds != null && !skillsIds.isEmpty()) ? skillsIds : null;

        Integer rateFilter = (rate != null && rate > 0) ? rate : null;

        Page<Talent> talentsPage = talentRepository.findAllActiveTalentsExcludingCompany(
                company.getId(), workModesFilter, skillsFilter, rateFilter, pageable);

        List<TalentResponseDto> dtoList = talentsPage.getContent().stream()
                .map(TalentMapper::toResponseDto)
                .toList();

        return new PageImpl<>(dtoList, pageable, talentsPage.getTotalElements());
    }


    @Override
    public TalentResponseDto getTalentById(Authentication authentication, Long talentId) {
        Talent talent = talentRepository.findById(talentId)
                .orElseThrow(() -> new NotFoundException("Talent with ID: " + talentId + " not found"));
        Company userCompany = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        if (!talent.getCompany().isTalentsSharedPublicly()) {
            validateTalentIsAvailableToCompany(userCompany, talent);
        }
        if (!talent.isActive() && !Objects.equals(talent.getCompany().getId(), userCompany.getId())) {
            throw new PermissionDeniedException("Talent is inactive");
        }
        return TalentMapper.toResponseDto(talent);
    }

    @Override
    public List<TalentResponseDto> getMyTalents(Authentication authentication) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        List<Talent> myTalents = talentRepository.findByCompanyId(company.getId());
        return myTalents.stream().map(TalentMapper::toResponseDto).toList();
    }

    @Override
    public void deleteTalent(Authentication authentication, Long id) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        Talent talent = getTalentOrThrow(id);
        validateTalentBelongsToCompany(company, talent);
        talentRepository.delete(talent);
    }

    @Override
    public void setTalentVisibility(Authentication authentication, TalentPublicityRequestDto requestDto) {
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        if (requestDto.isPublic()) {
            company.setTalentsSharedPublicly(true);
            company.setTalentAccessGroups(new HashSet<>());
        } else {
            if (requestDto.getPartnerGroupIds() == null || requestDto.getPartnerGroupIds().isEmpty()) {
                throw new InvalidRequestException("partnerGroupIds is null or empty");
            }
            Set<PartnerGroup> partnerGroupSet = new HashSet<>();
            for (Long id : requestDto.getPartnerGroupIds()) {
                PartnerGroup pg = partnerGroupRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Partner group with ID: " + id + " not found"));
                validatePartnerGroupBelongsToUserCompany(company, pg);
                partnerGroupSet.add(pg);
            }
            company.setTalentsSharedPublicly(false);
            company.setTalentAccessGroups(partnerGroupSet);
        }
        companyRepository.save(company);
    }

    @Override
    public TalentPublicityResponseDto getTalentVisibility(Authentication authentication) {
        User user = userService.getCurrentUserOrThrow(authentication);
        Company userCompany = user.getCompany();
        Set<Long> talentAccessGroupIds = new HashSet<>();
        if (!userCompany.isTalentsSharedPublicly()) {
            talentAccessGroupIds = userCompany.getTalentAccessGroups().stream()
                    .map(PartnerGroup::getId)
                    .collect(Collectors.toSet());
        }
        return TalentPublicityResponseDto.builder()
                .isPublic(userCompany.isTalentsSharedPublicly())
                .partnerGroupIds(talentAccessGroupIds)
                .build();
    }

    @Override
    public Company getCompanyOrThrow(Long id) {
        return companyRepository.findById(id).orElseThrow(() -> {
            log.warn("Company with id {} not found", id);
            return new NotFoundException("Company with ID: " + id + " not found");
        });
    }

    @Override
    public Company getUserCompanyOrThrow(User user) {
        Company userCompany = user.getCompany();
        if (userCompany == null) {
            throw new NotFoundException("User " + user.getUsername() + " is not associated with any company.");
        }
        return userCompany;
    }

    @Override
    public Talent getTalentOrThrow(Long talentId) {
        return talentRepository.findById(talentId)
                .orElseThrow(() -> new NotFoundException("Talent with ID: " + talentId + " not found"));
    }

    @Override
    public List<PartialTalentResponseDto> getMyTalentsPartial(Authentication authentication){
        Company company = getUserCompanyOrThrow(userService.getCurrentUserOrThrow(authentication));
        List<Talent> myTalents = talentRepository.findByCompanyId(company.getId());
        return TalentMapper.toPartialTalentList(myTalents);
    }

    private void setTalentRates(Talent talent, TalentRequestDto talentRequestDto) {
        Integer min = talentRequestDto.getMinRate();
        Integer max = talentRequestDto.getMaxRate();
        if (min == null || min <= 0) {
            throw new InvalidRequestException("Min rate must be greater than 0", "minRate");
        }
        if (max != null && min > max) {
            throw new InvalidRequestException("Min rate cannot exceed max rate", "minRate");
        }
        talent.setMinRate(min);
        talent.setMaxRate(max);
    }

    private void validateTalentIsAvailableToCompany(Company company, Talent talent) {
        boolean talentAvailable = talent.getCompany().getId().equals(company.getId()) ||
                talent.getCompany().getPartnerGroups().stream()
                        .flatMap(pg -> pg.getPartners().stream())
                        .anyMatch(c -> c.getId().equals(company.getId()));

        if (!talent.getCompany().getId().equals(company.getId()) && !talent.isActive()) {
            throw new PermissionDeniedException("Talent is not active.");
        }

        if (!talentAvailable) {
            throw new PermissionDeniedException("You have no access to this talent");
        }
    }

    private boolean hasSkillsChanged
            (List<SkillExperience> existingSkills, List<SkillExperienceRequestDto> newSkillsDtoList) {
        Set<String> existingSkillsSet = existingSkills.stream()
                .map(skillExp -> skillExp.getSkill().getId() + "-" +
                        skillExp.getMonths())
                .collect(Collectors.toSet());

        Set<String> newSkillsSet = newSkillsDtoList.stream()
                .map(dto -> dto.getSkillId() + "-" + dto.getMonths())
                .collect(Collectors.toSet());
        return !existingSkillsSet.equals(newSkillsSet);
    }

    private void updateTalentStatusAndInfo(TalentRequestDto dto, Talent talent) {
        talent.setDescription(dto.getDescription());
        setTalentLocations(talent, dto.getLocations());
        setTalentWorkModes(talent, dto.getWorkModes());
        talent.getTalentExperience().setPattern(getPatternOrThrow(dto.getExperience().getPatternId()));
        talent.getTalentExperience().setSeniority(getSeniorityOrThrow(dto.getExperience().getSeniorityId()));
        talent.getTalentExperience().setTotalTime(dto.getExperience().getTotalTime() != null ? dto.getExperience().getTotalTime() : 0);
        talent.setActive(dto.isActive());
    }


    private void setTalentLocations(Talent talent, List<Long> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) {
            talent.setLocations(new HashSet<>());
        } else {
            Set<Location> locations = new HashSet<>();
            for (Long locationId : locationIds) {
                Location l = locationRepository.findById(locationId)
                        .orElseThrow(() -> new NotFoundException("Location with ID: " + locationId + " not found"));
                locations.add(l);
            }
            talent.setLocations(locations);
        }
    }

    private void setTalentWorkModes(Talent talent, List<Long> workModeIds) {
        if (workModeIds == null || workModeIds.isEmpty()) {
            talent.setWorkModes(new HashSet<>());
        } else {
            Set<WorkMode> workModes = new HashSet<>();
            for (Long workModeId : workModeIds) {
                WorkMode wm = workModeRepository.findById(workModeId)
                        .orElseThrow(() -> new NotFoundException("Work mode with ID: " + workModeId + " not found"));
                workModes.add(wm);
            }
            talent.setWorkModes(workModes);
        }
    }

    protected void validateTalentBelongsToCompany(Company company, Talent talent) {
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
            throw new InvalidRequestException(user.getUsername() + " is already associated with Company: " + user.getCompany().getName());
        }
    }

    private EmailVerification setCompanyEmailVerificationStatus(Company userCompany, String userEmail, String newEmail) {
        EmailVerification status = EmailVerification.PENDING;
        if (userEmail.equals(newEmail)) {
            log.info("User email and company new email are the same. EmailVerification status will be ACCEPTED.");
            status = EmailVerification.ACCEPTED;
        }
        userCompany.setEmailVerification(status);
        log.info("Set EmailVerification status to {} for company with ID: {}", status, userCompany.getId());
        return status;
    }

    private CompanyResponseDto generateCompanyResponseDto(Company company) {
        CompanyResponseDto companyResponseDto = CompanyMapper.toCompanyResponseDto(company);
        companyResponseDto.setImage(imageService.returnUrlIfPictureExists(company.getId(), "image"));
        companyResponseDto.setBanner(imageService.returnUrlIfPictureExists(company.getId(), "banner"));
        if (company.getProjects() != null && !company.getProjects().isEmpty() && hasPositions(company.getProjects())) {
            companyResponseDto.setPositionViews(getProjectPositionsViews(company));
            companyResponseDto.setAcceptedApplications(getAcceptedApplications(company));
            companyResponseDto.setTotalApplications(getTotalApplications(company));
        }
        return companyResponseDto;
    }

    private boolean hasPositions(List<Project> projects) {
        return projects.stream()
                .anyMatch(project -> project.getPositions() != null && !project.getPositions().isEmpty());
    }

    private Long getProjectPositionsViews(Company company) {
        return company.getProjects().stream()
                .flatMap(project -> project.getPositions().stream())
                .mapToLong(Position::getViews)
                .sum();
    }

    private Long getAcceptedApplications(Company company) {
        return company.getProjects().stream()
                .flatMap(project -> project.getPositions().stream())
                .mapToLong(position -> positionApplicationRepository
                        .countByPositionIdAndApplicationStatus(position.getId(), ApplicationStatus.ACCEPTED))
                .sum();
    }

    private Long getTotalApplications(Company company) {
        return company.getProjects().stream()
                .flatMap(project -> project.getPositions().stream())
                .mapToLong(position -> positionApplicationRepository
                        .countByPositionIdExcludingAwaitingCvOrTalent(position.getId()))
                .sum();
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
        return new HashSet<>(skills);
    }

    private void updateCompanyName(Company userCompany, CompanyRequestDto companyRequestDto) {
        log.info("Attempting to update company name from {} to {}", userCompany.getName(), companyRequestDto.getName());
        String newName = companyRequestDto.getName();
        if (!newName.equals(userCompany.getName())) {
            if (companyRepository.findByNameIgnoreCase(newName).isPresent()) {
                throw new AlreadyExistsException("Company name '" + newName + "' already registered", "name");
            }
            userCompany.setName(newName);
            log.info("Company name updated from {} to {}", userCompany.getName(), companyRequestDto.getName());
        }
    }

    private boolean updateCompanyEmailIfChanged(Company userCompany, CompanyRequestDto companyRequestDto) {
        log.info("Attempting to update company (ID: {}) email from {} to {}", userCompany.getId(), userCompany.getEmail(), companyRequestDto.getEmail());
        String newEmail = companyRequestDto.getEmail();
        if (!newEmail.equals(userCompany.getEmail())) {
            if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent()) {
                throw new AlreadyExistsException("Email already registered", "email");
            }
            userCompany.setEmail(companyRequestDto.getEmail());
            log.info("Company (ID: {}) email updated from {} to {}", userCompany.getId(), userCompany.getEmail(), companyRequestDto.getEmail());
            return true; //mail was changed
        }
        log.info("Company (ID: {}) email not changed.", userCompany.getId());
        return false; //mail was not changed
    }

    private void setCompanyEmailVerificationStatusAndSendEmail(Company userCompany, User currentUser, CompanyRequestDto dto, HttpServletRequest request) {
        EmailVerification status = setCompanyEmailVerificationStatus(userCompany, currentUser.getEmail(), dto.getEmail());
        if (status.equals(EmailVerification.PENDING)) {
            String token = UUID.randomUUID().toString();
            userCompany.setEmailConfirmationToken(token);
            companyRepository.save(userCompany);
            log.info("Created new token for company with ID: {}", userCompany.getId());
            mailService.sendCompanyEmailConfirmation(userCompany, token, request);
        }
    }

    private void updateCompanyWebsiteAndLinkedIn(Company userCompany, CompanyRequestDto companyRequestDto) {
        String newWebsite = companyRequestDto.getWebsite();
        if (newWebsite != null && !newWebsite.isEmpty() && !newWebsite.equals(userCompany.getWebsite())) {
            if (companyRepository.findByWebsite(newWebsite).isPresent()) {
                throw new AlreadyExistsException("Website already registered", "website");
            }
            log.info("Company ID: {} - changed website to {} ", userCompany.getId(), newWebsite);
            userCompany.setWebsite(newWebsite);
        }

        String newLinkedIn = companyRequestDto.getLinkedIn();
        if (newLinkedIn != null && !newLinkedIn.isEmpty() && !newLinkedIn.equals(userCompany.getLinkedIn())) {
            if (companyRepository.findByLinkedIn(newLinkedIn).isPresent()) {
                throw new AlreadyExistsException("LinkedIn already registered", "linkedIn");
            }
            log.info("Company ID: {} - changed linkedIn to {} ", userCompany.getId(), newLinkedIn);
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
            log.info("Company ID: {} - changed company type to {} ", company.getId(), getCompanyTypeOrThrow(dto));
        }
        if (dto.getDomainId() != null && !company.getDomain().getId().equals(dto.getDomainId())) {
            company.setDomain(getDomainOrThrow(dto));
            log.info("Company ID: {} - changed domain to {} ", company.getId(), getDomainOrThrow(dto));
        }
        List<Long> companySkills = company.getSkills().stream().map(Skill::getId).toList();
        if (!companySkills.equals(dto.getSkills())) {
            company.setSkills(getSkillsOrThrow(dto));
            log.info("Company ID: {} - changed skills to {} ", company.getId(), getSkillsOrThrow(dto).stream().toList());
        }
        if (dto.getDescription() != null && !dto.getDescription().isEmpty() && !dto.getDescription().equals(company.getDescription())) {
            company.setDescription(dto.getDescription());
            log.info("Company ID: {} - changed description to {} ", company.getId(), dto.getDescription());
        }
    }

    private void validateCompanyRequestInput(CompanyRequestDto companyRequestDto) {
        if (companyRepository.findByNameIgnoreCase(companyRequestDto.getName()).isPresent())
            throw new AlreadyExistsException("Name already registered", "name");
        if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent())
            throw new AlreadyExistsException("Email already registered", "email");

        String website = companyRequestDto.getWebsite();
        if (website != null && !website.isEmpty()) {
            if (companyRepository.findByWebsite(website).isPresent())
                throw new AlreadyExistsException("Website already registered", "website");
        }

        String linkedIn = companyRequestDto.getLinkedIn();
        if (linkedIn != null && !linkedIn.isEmpty()) {
            if (companyRepository.findByLinkedIn(linkedIn).isPresent())
                throw new AlreadyExistsException("LinkedIn already registered", "linkedIn");
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
        return companyIds.stream().map(this::getCompanyOrThrow)
                .collect(Collectors.toSet());
    }

    private void setTalentExperience(TalentExperienceRequestDto dto, Talent talent) {
        TalentExperience talentExperience = createTalentExperience(dto, talent);
        List<SkillExperience> skillExperienceList = processSkillExperiences(dto.getSkills(), talentExperience);
        talentExperience.setSkillExperienceList(skillExperienceList);
        talent.setTalentExperience(talentExperience);
        talentExperienceRepository.save(talentExperience);
    }

    private TalentExperience createTalentExperience(TalentExperienceRequestDto dto, Talent talent) {
        TalentExperience talentExperience = new TalentExperience();
        talentExperience.setTalent(talent);
        talentExperience.setTotalTime(dto.getTotalTime() != null ? dto.getTotalTime() : 0);
        talentExperience.setPattern(getPatternOrThrow(dto.getPatternId()));
        talentExperience.setSeniority(getSeniorityOrThrow(dto.getSeniorityId()));
        return talentExperience;
    }

    private List<SkillExperience> processSkillExperiences(List<SkillExperienceRequestDto> skillExperienceRequests, TalentExperience talentExperience) {
        List<SkillExperience> skillExperienceList = new ArrayList<>();
        if (skillExperienceRequests != null) {
            for (SkillExperienceRequestDto dtoItem : skillExperienceRequests) {
                Skill skill = getSkillOrThrow(dtoItem.getSkillId());
                if (isSkillIsAlreadyInList(skillExperienceList, skill)) {
                    continue;
                }
                skillExperienceList.add(createSkillExperience(talentExperience, skill, dtoItem.getMonths()));
            }
            return skillExperienceList;
        } else {
            return new ArrayList<>();
        }
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
        if (talentRequestDto.getExperience().getSkills() != null) {
            List<SkillExperienceRequestDto> skillExperienceList = talentRequestDto.getExperience().getSkills();
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
    }

    private SkillExperience createSkillExperience(TalentExperience talentExperience, Skill skill, Integer experience) {
        return SkillExperience.builder()
                .talentExperience(talentExperience)
                .skill(skill)
                .months(experience)
                .build();
    }

    private Skill getSkillOrThrow(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill with ID: " + skillId + " not found"));
    }

    protected Pattern getPatternOrThrow(Long patternId) {
        return patternRepository.findById(patternId)
                .orElseThrow(() -> new NotFoundException("Pattern with ID: " + patternId + " not found"));
    }

    private Seniority getSeniorityOrThrow(Long seniorityId) {
        return seniorityRepository.findById(seniorityId)
                .orElseThrow(() -> new NotFoundException("Seniority with ID: " + seniorityId + " not found"));
    }

    private void disableDefaultFilterIfExistsAndHasNoSkills(Company c) {
        Filter defaultFilter = c.getFilters().stream()
                .filter(f -> f.getName().equalsIgnoreCase("Default") && f.getIsEnabled() && f.getSkills().isEmpty())
                .findFirst()
                .orElse(null);
        // Disable only if the 'Default' filter exists, is Enabled and has No skills (which makes it custom)
        if (defaultFilter != null) {
            defaultFilter.setIsEnabled(false);
            filterRepository.save(defaultFilter);
        }
    }

    private void createDefaultFilterIfCompanyHasNoSkills(Company company) {
        Set<Skill> skills = company.getSkills();
        if (skills.isEmpty()) {
            Filter filter = Filter.builder()
                    .name("Default")
                    .isEnabled(true)
                    .company(company)
                    .build();
            filterRepository.save(filter);
            company.setFilters(new HashSet<>(Set.of(filter)));
            companyRepository.save(company);
            log.info("Created default filter for company {}  (ID: {})", company.getName(), company.getId());
        }
    }
}