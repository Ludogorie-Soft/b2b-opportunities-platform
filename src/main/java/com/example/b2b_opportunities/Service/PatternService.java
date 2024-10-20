package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.PatternRequestDto;
import com.example.b2b_opportunities.Dto.Response.PatternResponseDto;
import com.example.b2b_opportunities.Entity.Pattern;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Exception.common.DuplicateResourceException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.PatternMapper;
import com.example.b2b_opportunities.Repository.PatternRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatternService {
    private final SkillRepository skillRepository;
    private final PatternRepository patternRepository;

    public PatternResponseDto get(Long id) {
        return PatternMapper.toDto(getPatternIfExists(id));
    }

    public List<PatternResponseDto> getAll() {
        return PatternMapper.toDtoList(patternRepository.findAll());
    }

    public PatternResponseDto create(PatternRequestDto dto) {
        return createOrUpdate(dto, new Pattern());
    }

    public PatternResponseDto update(PatternRequestDto dto) {
        Pattern pattern = getPatternIfExists(dto.getId());
        return createOrUpdate(dto, pattern);
    }

    public void delete(Long id) {
        getPatternIfExists(id);
        patternRepository.deleteById(id);
    }

    private PatternResponseDto createOrUpdate(PatternRequestDto dto, Pattern pattern) {
        String newName = dto.getName();
        checkIfPatternNameAlreadyExistsForAnotherID(newName, dto.getId());

        pattern.setName(newName);
        pattern.setParent(getParentIfExists(dto.getParentId()));
        pattern.setSuggestedSkills(getAllSkillsIfSkillIdsExist(dto.getSuggestedSkills()));
        return PatternMapper.toDto(patternRepository.save(pattern));
    }


    // TODO: move this method to a better place (make SET)
    public List<Skill> getAllSkillsIfSkillIdsExist(List<Long> skillList) {
        return getAllSkillsIfSkillIdsExist(skillList, false);
    }

    public List<Skill> getAllAssignableSkillsIfSkillIdsExist(List<Long> skillList) {
        return getAllSkillsIfSkillIdsExist(skillList, true);
    }

    public List<Skill> getAllSkillsIfSkillIdsExist(List<Long> skillIds, boolean getAssignableOnly) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Skill> skills = skillRepository.findAllById(skillIds);

        // Check for missing skill IDs
        List<Long> missingSkillIds = skillIds.stream()
                .filter(id -> skills.stream().noneMatch(skill -> skill.getId().equals(id)))
                .toList();

        if (!missingSkillIds.isEmpty()) {
            throw new NotFoundException("Skills with ID(s) " + missingSkillIds + " not found.");
        }

        // Check if skills can be assigned
        if (getAssignableOnly) {
            List<Long> nonAssignableSkillIds = skills.stream()
                    .filter(skill -> Boolean.FALSE.equals(skill.getAssignable()))
                    .map(Skill::getId)
                    .toList();

            if (!nonAssignableSkillIds.isEmpty()) {
                throw new NotFoundException("Skills with ID(s) " + nonAssignableSkillIds + " cannot be assigned.");
            }
        }

        return skills;
    }

    private void checkIfPatternNameAlreadyExistsForAnotherID(String name, Long id) {
        Optional<Pattern> optionalPattern = patternRepository.findByName(name);

        if (optionalPattern.isPresent()) {
            if (Objects.equals(id, null) || !Objects.equals(optionalPattern.get().getId(), id)) {
                throw new DuplicateResourceException("Pattern with name: '" + name + "' already exists.");
            }
        }
    }

    private Pattern getParentIfExists(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return patternRepository.findById(parentId).orElseThrow(
                () -> new NotFoundException("Parent with pattern ID: " + parentId + " not found."));
    }

    private Pattern getPatternIfExists(Long id) {
        return patternRepository.findById(id).orElseThrow(() -> new NotFoundException("Pattern with ID: " + id + " not found"));
    }
}
