package com.example.b2b_opportunities.dto.requestDtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PatternRequestDto {
    private Long id;

    @NotBlank()
    private String name;

    private List<Long> suggestedSkills;
    private Long parentId;
}
