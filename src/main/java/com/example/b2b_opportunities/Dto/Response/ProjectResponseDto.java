package com.example.b2b_opportunities.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
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
    private List<Long> partnerGroupIds;
}
