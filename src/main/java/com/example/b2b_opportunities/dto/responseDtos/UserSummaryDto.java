package com.example.b2b_opportunities.dto.responseDtos;

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
public class UserSummaryDto {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Long companyId;
    private LocalDateTime lastLogin;
}
