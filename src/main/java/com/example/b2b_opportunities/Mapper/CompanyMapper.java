package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompanyPublicResponseDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Skill;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CompanyMapper {

    public static Company toCompany(CompanyRequestDto companyRequestDto) {

        return Company.builder()
                .name(companyRequestDto.getName())
                .email(companyRequestDto.getEmail())
                .website(companyRequestDto.getWebsite())
                .linkedIn(companyRequestDto.getLinkedIn())
                .description(companyRequestDto.getDescription())
                .users(new ArrayList<>())
                .build();
    }

    public static CompanyPublicResponseDto toCompanyPublicResponseDto(Company company) {
        return CompanyPublicResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .build();
    }

    public static List<CompanyPublicResponseDto> toCompanyPublicResponseDtoList(List<Company> companies) {
        return companies.stream()
                .map(CompanyMapper::toCompanyPublicResponseDto)
                .collect(Collectors.toList());
    }

    public static CompanyResponseDto toCompanyResponseDto(Company company) {
        List<Long> skillIDs = toSkillIdList(company.getSkills());

        String description = company.getDescription();
        if (description != null) {
            description = company.getDescription().replace("\\n", "\n");
        }

        return CompanyResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .email(company.getEmail())
                .companyType(company.getCompanyType())
                .domain(company.getDomain())
                .emailVerification(company.getEmailVerification().toString())
                .website(company.getWebsite())
                .linkedIn(company.getLinkedIn())
                .description(description)
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

    public static List<CompanyResponseDto> toCompanyResponseDtoList(List<Company> companyList) {
        List<CompanyResponseDto> responseDtoList = new ArrayList<>();
        for (Company company : companyList) {
            responseDtoList.add(toCompanyResponseDto(company));
        }
        return responseDtoList;
    }

    public static Set<CompanyResponseDto> toCompanyResponseDtoSet(Set<Company> companyList) {
        Set<CompanyResponseDto> responseDtoSet = new HashSet<>();
        for (Company company : companyList) {
            responseDtoSet.add(toCompanyResponseDto(company));
        }
        return responseDtoSet;
    }
}