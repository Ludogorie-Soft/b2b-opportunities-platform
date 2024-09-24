package com.example.b2b_opportunities.Dto.Response;

import com.example.b2b_opportunities.Entity.Skill;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillResponseDto {
    private Long id;
    private String identifier;
    private String name;
    private String imageType;
    private String imageBase64;
    private Skill parent;
    private Boolean assignable;
}
