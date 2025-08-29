package com.example.b2b_opportunities.mapper;

import com.example.b2b_opportunities.dto.responseDtos.ProjectResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.ProjectStatsDto;
import com.example.b2b_opportunities.entity.PartnerGroup;
import com.example.b2b_opportunities.entity.Project;

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
                .description(project.getDescription())
                .canReactivate(project.isCanReactivate())
                .status(project.getProjectStatus().toString())
                .isPartnerOnly(project.isPartnerOnly())
                .partnerGroups(project.isPartnerOnly() ?
                        project.getPartnerGroupList().stream().map(PartnerGroup::getId)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    public static ProjectResponseDto toDtoWithPrivateNotes(Project project){
        ProjectResponseDto dto = toDto(project);
        dto.setPrivateNotes(project.getPrivateNotes());
        return dto;
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

    public static ProjectStatsDto toProjectStatsDto(Project project){
        return ProjectStatsDto.builder()
                .id(project.getId())
                .companyName(project.getCompany().getName())
                .positionCount(project.getPositions().size())
                .createdAt(project.getDatePosted())
                .updatedAt(project.getDateUpdated())
                .build();
    }
}
