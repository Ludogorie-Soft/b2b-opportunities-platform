package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Response.SkillResponseDto;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Exception.SkillNotFoundException;
import com.example.b2b_opportunities.Mapper.SkillMapper;
import com.example.b2b_opportunities.Repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/skills")
public class SkillController {

    private final SkillRepository skillRepository;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<SkillResponseDto> getSkills() {
        List<Skill> skills = skillRepository.findAll();
        return SkillMapper.skillResponseDtoList(skills);
    }

    @GetMapping("/id/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SkillResponseDto getSkillById(@PathVariable(name = "id") Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException("Skill with id '" + id + "' not found"));
        return SkillMapper.toResponseDto(skill);
    }

    @GetMapping("/{identifier}")
    @ResponseStatus(HttpStatus.OK)
    public SkillResponseDto getSkillByIdentifier(@PathVariable(name = "identifier") String identifier) {
        Skill skill = skillRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new SkillNotFoundException("Skill with identifier '" + identifier + "' not found"));
        return SkillMapper.toResponseDto(skill);
    }
}