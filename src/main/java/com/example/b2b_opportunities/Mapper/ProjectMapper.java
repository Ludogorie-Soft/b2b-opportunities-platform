package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectMapper {
    public static ProjectResponseDto toDto(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .companyId(project.getCompany().getId())
                .datePosted(project.getDatePosted())
                .expiryDate(project.getExpiryDate())
                .name(project.getName())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .duration(project.getDuration())
                .Description(project.getDescription())
                .status(project.getProjectStatus().toString())
                .isPartnerOnly(project.isPartnerOnly())
                .partnerGroups(project.isPartnerOnly() ?
                        project.getPartnerGroupList().stream().map(PartnerGroup::getId)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    public static List<ProjectResponseDto> toDtoList(List<Project> projects) {
        List<ProjectResponseDto> projectResponseDtos = new ArrayList<>();
        if (projects != null && !projects.isEmpty()) {
            for (Project p : projects) {
                projectResponseDtos.add(toDto(p));
            }
            return projectResponseDtos;
        }
        return null;
    }

    public static Set<ProjectResponseDto> toDtoSet(Set<Project> projects) {
        Set<ProjectResponseDto> projectResponseDtos = new HashSet<>();
        if (projects != null && !projects.isEmpty()) {
            for (Project p : projects) {
                projectResponseDtos.add(toDto(p));
            }
            return projectResponseDtos;
        }
        return null;
    }
}
