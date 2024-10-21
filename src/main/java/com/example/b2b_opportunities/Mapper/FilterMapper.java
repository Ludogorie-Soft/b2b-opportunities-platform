package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Request.CompanyFilterEditDto;
import com.example.b2b_opportunities.Dto.Request.CompanyFilterRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompanyFilterResponseDto;
import com.example.b2b_opportunities.Entity.Filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterMapper {
    public static CompanyFilterResponseDto toDto(Filter filter) {
        return CompanyFilterResponseDto.builder()
                .id(filter.getId())
                .name(filter.getName())
                .isEnabled(filter.getIsEnabled())
                // TODO: use SET instead of List for all skills
                .skills(new HashSet<>(SkillMapper.toSkillResponseNoParentsDtoList(filter.getSkills().stream().toList())))
                .build();
    }

    public static List<CompanyFilterResponseDto> toDtoList(Set<Filter> filters) {
        return filters.stream()
                .map(FilterMapper::toDto)
                .collect(Collectors.toList());
    }

    public static Filter toEntity (CompanyFilterRequestDto dto){
        Boolean enabled = true; // Enabled when creating new DTO

        if (dto instanceof CompanyFilterEditDto) {
            enabled = ((CompanyFilterEditDto) dto).getIsEnabled();
        }

        return Filter.builder()
                .name(dto.getName())
                // skills to be added manually after
                .isEnabled(enabled)
                .build();
    }
}
