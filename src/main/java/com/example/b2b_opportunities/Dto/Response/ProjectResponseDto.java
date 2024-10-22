package com.example.b2b_opportunities.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponseDto {
    private Long id;

    private Long companyId;

    private LocalDateTime datePosted;
    private String name;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer duration; // months

    private String Description;
    private String status;
    private boolean isPartnerOnly;
}
