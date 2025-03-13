package com.example.b2b_opportunities.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PositionApplicationResponseDto {
    private Long id;
    private Long positionId;
    private Long talentId;
    private String applicationStatus;
    private LocalDateTime applicationDateTime;
    private LocalDateTime lastUpdateDateTime;
    private LocalDateTime availableFrom;
    private int rate;
    private String cvUrl;
    private Long companyId;
    private String companyName;
    private String companyImage;
}
