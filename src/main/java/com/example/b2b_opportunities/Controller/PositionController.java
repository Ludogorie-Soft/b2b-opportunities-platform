package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.PositionEditRequestDto;
import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Service.Interface.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService positionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PositionResponseDto createPosition(@RequestBody @Valid PositionRequestDto dto, Authentication authentication) {
        return positionService.createPosition(dto, authentication);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Set<PositionResponseDto> getPositions(Authentication authentication) {
        return positionService.getPositions(authentication);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionResponseDto getPosition(Authentication authentication, @PathVariable("id") Long id) {
        return positionService.getPosition(authentication, id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionResponseDto editPosition(@PathVariable("id") Long id, @RequestBody @Valid PositionEditRequestDto positionRequestDto, Authentication authentication) {
        return positionService.editPosition(id, positionRequestDto, authentication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePosition(@PathVariable("id") Long id, Authentication authentication) {
        positionService.deletePosition(id, authentication);
    }

    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public void editPositionStatus(
            @PathVariable("id") Long positionId,
            @RequestParam Long statusId,
            @RequestParam(required = false) String customCloseReason,
            Authentication authentication
    ) {
        positionService.editPositionStatus(positionId, statusId, customCloseReason, authentication);
    }
}