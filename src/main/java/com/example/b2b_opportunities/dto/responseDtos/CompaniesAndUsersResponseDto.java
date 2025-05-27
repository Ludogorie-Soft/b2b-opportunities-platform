package com.example.b2b_opportunities.dto.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompaniesAndUsersResponseDto {
    private CompanyResponseDto company;
    private List<UserResponseDto> users;
}
