package com.example.b2b_opportunities.Dto.Request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkmodeRequestDto {
    @NotEmpty
    @Size(min = 2, max = 50)
    private String name;
}
