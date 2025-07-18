package com.example.b2b_opportunities.mapper;

import com.example.b2b_opportunities.dto.requestDtos.PositionRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.PartialPositionResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.PositionResponseDto;
import com.example.b2b_opportunities.entity.Position;
import com.example.b2b_opportunities.entity.Skill;
import com.example.b2b_opportunities.entity.WorkMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PositionMapper {
    public static Position toPosition(PositionRequestDto positionRequestDto) {
        return Position.builder()
                .minYearsExperience(positionRequestDto.getMinYearsExperience())
                .hoursPerWeek(positionRequestDto.getHoursPerWeek())
                .responsibilities(positionRequestDto.getResponsibilities())
                .hiringProcess(positionRequestDto.getHiringProcess())
                .description(positionRequestDto.getDescription())
                .build();
    }

    public static PositionResponseDto toResponseDto(Position position) {
        Set<Long> workModeList = new HashSet<>(position.getWorkModes().stream().map(WorkMode::getId).toList());

        List<Long> optionalSkillIds = new ArrayList<>();
        if (position.getOptionalSkills() != null) {
            optionalSkillIds = position.getOptionalSkills().stream().map(Skill::getId).toList();
        }

        return PositionResponseDto.builder()
                .id(position.getId())
                .projectId(position.getProject().getId())
                .patternId(position.getPattern().getId())
                .seniority(position.getSeniority().getLevel())
                .workMode(workModeList)
                .rate(position.getRate().getCurrency().getId() != 4L ? RateMapper.toRateResponseDto(position.getRate()) : null)
                .requiredSkills(RequiredSkillMapper.toRequiredSkillResponseDtoList(position.getRequiredSkills()))
                .optionalSkills(optionalSkillIds)
                .minYearsExperience(position.getMinYearsExperience())
                .location(position.getLocation() != null ? position.getLocation().getId() : null)
                .hoursPerWeek(position.getHoursPerWeek())
                .responsibilities(position.getResponsibilities())
                .hiringProcess(position.getHiringProcess())
                .description(position.getDescription())
                .statusId(position.getStatus().getId())
                .customCloseReason(position.getCustomCloseReason())
                .build();
    }

    public static List<PositionResponseDto> toResponseDtoList(List<Position> positions){
        List<PositionResponseDto> positionResponseDtoList = new ArrayList<>();
        for (Position position : positions) {
            positionResponseDtoList.add(PositionMapper.toResponseDto(position));
        }
        return positionResponseDtoList;
    }

    public static Set<PositionResponseDto> toResponseDtoSet(Set<Position> positions){
        Set<PositionResponseDto> positionResponseDtoList = new HashSet<>();
        for (Position position : positions) {
            positionResponseDtoList.add(PositionMapper.toResponseDto(position));
        }
        return positionResponseDtoList;
    }

    public static PartialPositionResponseDto toPartialPositionResponseDto(Position position){
        return PartialPositionResponseDto.builder()
                .id(position.getId())
                .seniorityId(position.getSeniority().getId())
                .patternId(position.getPattern().getId())
                .projectName(position.getProject().getName())
                .build();
    }

}