package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Request.ProjectStatusUpdateRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService positionService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public PositionResponseDto createPosition(@RequestBody @Valid PositionRequestDto dto, Authentication authentication) {
        return positionService.createPosition(dto, authentication);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PositionResponseDto> getPositions() {
        return positionService.getPositions();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionResponseDto getPosition(@PathVariable("id") Long id) {
        return positionService.getPosition(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionResponseDto editPosition(@PathVariable("id") Long id, @RequestBody @Valid PositionRequestDto positionRequestDto, Authentication authentication) {
        return positionService.editPosition(id, positionRequestDto, authentication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePosition(@PathVariable("id") Long id, Authentication authentication) {
        positionService.deletePosition(id, authentication);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public void editPositionStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProjectStatusUpdateRequestDto statusUpdateRequestDto,
            Authentication authentication
    ) {
        positionService.editPositionStatus(id, statusUpdateRequestDto.getStatus(), authentication);
    }
}