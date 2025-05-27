package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.entity.PositionStatus;
import com.example.b2b_opportunities.exception.common.NotFoundException;
import com.example.b2b_opportunities.repository.PositionStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/position-statuses")
@RequiredArgsConstructor
public class PositionStatusController {
    private final PositionStatusRepository positionStatusRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PositionStatus> get() {
        return positionStatusRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionStatus getById(@PathVariable("id") Long id) {
        return positionStatusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Position status with ID: " + id + " not found"));
    }
}
