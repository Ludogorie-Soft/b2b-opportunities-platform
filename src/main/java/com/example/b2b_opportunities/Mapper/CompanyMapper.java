package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import com.example.b2b_opportunities.Repository.DomainRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CompanyMapper {

    private final SkillRepository skillRepository;
    private final CompanyTypeRepository companyTypeRepository;
    private final DomainRepository domainRepository;

    public CompanyMapper(SkillRepository skillRepository, CompanyTypeRepository companyTypeRepository, DomainRepository domainRepository) {
        this.skillRepository = skillRepository;
        this.companyTypeRepository = companyTypeRepository;
        this.domainRepository = domainRepository;
    }

    public Company toCompany(CompanyRequestDto companyRequestDto) {
        Set<Skill> skills = skillRepository.findAllByIdIn(companyRequestDto.getSkills());
        CompanyType companyType = companyTypeRepository.findById(companyRequestDto.getCompanyTypeId()).orElseThrow(() -> new NotFoundException("Company type not found"));
        Domain domain = domainRepository.findById(companyRequestDto.getDomainId()).orElseThrow(() -> new NotFoundException("Domain not found"));

        return Company.builder()
                .name(companyRequestDto.getName())
                .email(companyRequestDto.getEmail())
                .companyType(companyType)
                .website(companyRequestDto.getWebsite())
//                .emailVerification(companyRequestDto.getEmailVerification()) -> will be set in the service
                .domain(domain)
                .linkedIn(companyRequestDto.getLinkedIn())
//                .users(companyRequestDto.getUsers()) -> initially it will set the current logged-in user
                .description(companyRequestDto.getDescription())
                .skills(skills)
                .image("no-image")
                .users(new ArrayList<>())
                .build();
    }

    public static CompanyResponseDto toCompanyResponseDto(Company company) {
        List<Long> skillIDs = toSkillIdList(company.getSkills());
        return CompanyResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .email(company.getEmail())
                .companyType(company.getCompanyType().getName())
                .domain(company.getDomain().getName())
                .emailVerification(company.getEmailVerification().toString())
                .website(company.getWebsite())
                .linkedIn(company.getLinkedIn())
                .image(company.getImage())
                .banner(company.getBanner())
                .description(company.getDescription())
                .skills(skillIDs)
                .build();
    }
    private static List<Long> toSkillIdList(Set<Skill> skillSet) {
        List<Long> skillIdList = new ArrayList<>();
        for (Skill s : skillSet) {
            skillIdList.add(s.getId());
        }
        return skillIdList;
    }

    public static List<CompanyResponseDto> toCompanyResponseDtoList(List<Company> companyList){
        List<CompanyResponseDto> responseDtoList = new ArrayList<>();
        for(Company company: companyList){
            responseDtoList.add(CompanyMapper.toCompanyResponseDto(company));
        }
        return responseDtoList;
    }

}
