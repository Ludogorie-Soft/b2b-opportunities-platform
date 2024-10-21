package com.example.b2b_opportunities.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillResponseNoParentsDto {
    private Long id;
    private String name;
    private String imageType;
    private String imageBase64;
}
