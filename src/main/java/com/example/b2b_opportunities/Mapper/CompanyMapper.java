package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.SkillRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CompanyMapper {

    private static SkillRepository skillRepository;

    public static Company toCompany(CompanyRequestDto companyRequestDto) {
        return Company.builder()
                .name(companyRequestDto.getName())
                .email(companyRequestDto.getEmail())
                .companyType(companyRequestDto.getCompanyType())
                .website(companyRequestDto.getWebsite())
//                .emailVerification(companyRequestDto.getEmailVerification()) -> will be set in the service
                .domain(companyRequestDto.getDomain())
                .linkedIn(companyRequestDto.getLinkedIn())
//                .users(companyRequestDto.getUsers()) -> initially it will set the current logged-in user
                .description(companyRequestDto.getDescription())
                .skills(toSkillList(companyRequestDto.getSkills()))
                .build();
    }

    public static CompanyResponseDto toCompanyResponseDto(Company company) {
        return CompanyResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .email(company.getEmail())
                .companyType(company.getCompanyType().toString())
                .website(company.getWebsite())
                .image(company.getImage())
                .emailVerification(company.getEmailVerification().toString())
                .domain(company.getDomain().toString())
                .linkedIn(company.getLinkedIn())
                .banner(company.getBanner())
                .description(company.getDescription())
                .skills(toSkillIdList(company.getSkills()))
                .users(getUserIds(company.getUsers()))
                .build();
    }

    private static List<Long> getUserIds(List<User> usersList) {
        List<Long> idList = new ArrayList<>();
        for (User u : usersList) {
            idList.add(u.getId());
        }
        return idList;
    }

    private static Set<Skill> toSkillList(Set<Long> skillSet) {
        Set<Skill> skillList = new HashSet<>();
        for (Long skillId : skillSet) {
            skillList.add(skillRepository.findById(skillId).orElseThrow());
        }
        return skillList;
    }

    private static List<Long> toSkillIdList(Set<Skill> skillSet) {
        List<Long> skillIdList = new ArrayList<>();
        for(Skill s:skillSet){
            skillIdList.add(s.getId());
        }
        return skillIdList;
    }

}
