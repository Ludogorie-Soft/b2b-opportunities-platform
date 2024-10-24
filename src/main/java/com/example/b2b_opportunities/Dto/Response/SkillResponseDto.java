package com.example.b2b_opportunities.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillResponseDto extends SkillResponseNoParentsDto{
    private SkillResponseDto parent;
    private Boolean assignable;
}
