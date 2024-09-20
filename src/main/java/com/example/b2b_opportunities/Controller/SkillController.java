package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/skills")
public class SkillController {

    private final SkillRepository skillRepository;

    @GetMapping("/get")
    public ResponseEntity<List<Skill>> getSkills(){
        return ResponseEntity.ok(skillRepository.findAll());
    }
}
