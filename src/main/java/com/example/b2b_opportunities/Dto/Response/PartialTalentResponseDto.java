package com.example.b2b_opportunities.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartialTalentResponseDto {
    private Long id;
    private Long seniorityId;
    private Long patternId;
}
