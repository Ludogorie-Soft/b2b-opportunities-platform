package com.example.b2b_opportunities.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyPositionsResponseDto {
    private Long positionId;
    private Long seniorityId;
    private Long patternId;
    private String projectName;
}
