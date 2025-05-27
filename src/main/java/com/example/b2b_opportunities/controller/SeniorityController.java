package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.entity.Seniority;
import com.example.b2b_opportunities.exception.common.NotFoundException;
import com.example.b2b_opportunities.repository.SeniorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seniorities")
@RequiredArgsConstructor
public class SeniorityController {

    private final SeniorityRepository seniorityRepository;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<Seniority> getSeniorities() {
        return seniorityRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Seniority getSeniority(@PathVariable("id") Long id) {
        return seniorityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Seniority with id '" + id + "' not found"));
    }
}
