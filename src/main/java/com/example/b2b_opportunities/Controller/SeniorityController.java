package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.Seniority;
import com.example.b2b_opportunities.Repository.SeniorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seniorities")
@RequiredArgsConstructor
public class SeniorityController {

    private final SeniorityRepository seniorityRepository;

    @GetMapping("/get")
    public ResponseEntity<List<Seniority>> getSeniorities(){
        return ResponseEntity.ok(seniorityRepository.findAll());
    }
}
