package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.dto.requestDtos.PatternRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.PatternResponseDto;
import com.example.b2b_opportunities.services.interfaces.PatternService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@RestController
@RequestMapping("/patterns")
@RequiredArgsConstructor
public class PatternController {
    private final PatternService patternService;

    //TODO - change POST/PUT endpoints in SecurityConfig to be accessible to admins only
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PatternResponseDto get(@PathVariable("id") Long id) {
        return patternService.get(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PatternResponseDto> getAll() {
        return patternService.getAll();
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public PatternResponseDto update(@RequestBody PatternRequestDto patternRequestDto) {
        return patternService.update(patternRequestDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatternResponseDto create(@RequestBody @Valid PatternRequestDto patternRequestDto) {
        return patternService.create(patternRequestDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam Long id) {
        patternService.delete(id);
    }
}
