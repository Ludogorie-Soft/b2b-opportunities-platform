package com.example.b2b_opportunities.Mapper;

import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.WorkMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PositionMapper {
    public static Position toPosition(PositionRequestDto positionRequestDto) {
        return Position.builder()
                .isActive(positionRequestDto.getIsActive())
                .minYearsExperience(positionRequestDto.getMinYearsExperience())
                .hoursPerWeek(positionRequestDto.getHoursPerWeek())
                .responsibilities(positionRequestDto.getResponsibilities())
                .hiringProcess(positionRequestDto.getHiringProcess())
                .description(positionRequestDto.getDescription())
                .build();
    }

    public static PositionResponseDto toResponseDto(Position position) {
        List<String> workModeList = new ArrayList<>(position.getWorkModes().stream().map(WorkMode::getName).toList());
        List<Long> optionalSkillIds = new ArrayList<>();
        if (position.getOptionalSkills() != null) {
            optionalSkillIds = position.getOptionalSkills().stream().map(Skill::getId).toList();
        }

        return PositionResponseDto.builder()
                .id(position.getId())
                .projectId(position.getProject().getId())
//                .role(position.getRole().getId())
                .patternId(position.getPattern().getId())
                .isActive(position.getIsActive())
                .seniority(position.getSeniority().getLevel())
                .workMode(workModeList)
                .rate(RateMapper.toRateResponseDto(position.getRate()))
                .requiredSkills(RequiredSkillMapper.toRequiredSkillResponseDtoList(position.getRequiredSkills()))
                .optionalSkills(optionalSkillIds)
                .minYearsExperience(position.getMinYearsExperience())
                .location(position.getLocation() != null ? position.getLocation().getId() : null)
                .hoursPerWeek(position.getHoursPerWeek())
                .responsibilities(position.getResponsibilities())
                .hiringProcess(position.getHiringProcess())
                .description(position.getDescription())
                .build();
    }

    public static List<PositionResponseDto> toResponseDtoList(List<Position> positions){
        List<PositionResponseDto> positionResponseDtoList = new ArrayList<>();
        for (Position position : positions) {
            positionResponseDtoList.add(PositionMapper.toResponseDto(position));
        }
        return positionResponseDtoList;
    }
}