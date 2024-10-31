package com.example.b2b_opportunities.Dto.Request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ProjectRequestDto {
    private String name;

    private LocalDate startDate;
    private LocalDate endDate; // YYYY-MM-DD
    private Integer duration; // months

    private String Description;
    private boolean isPartnerOnly;
    private List<Long> partnerGroups; //if isPartnerOnly = true
}
