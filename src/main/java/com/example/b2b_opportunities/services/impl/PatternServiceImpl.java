package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.dto.requestDtos.PatternRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.PatternResponseDto;
import com.example.b2b_opportunities.entity.Pattern;
import com.example.b2b_opportunities.entity.Skill;
import com.example.b2b_opportunities.exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.exception.common.NotFoundException;
import com.example.b2b_opportunities.mapper.PatternMapper;
import com.example.b2b_opportunities.repository.PatternRepository;
import com.example.b2b_opportunities.repository.SkillRepository;
import com.example.b2b_opportunities.services.interfaces.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatternServiceImpl implements PatternService {
    private final SkillRepository skillRepository;
    private final PatternRepository patternRepository;

    @Override
    public PatternResponseDto get(Long id) {
        return PatternMapper.toDto(getPatternIfExists(id));
    }

    @Override
    public List<PatternResponseDto> getAll() {
        return PatternMapper.toDtoList(patternRepository.findAll());
    }

    @Override
    public PatternResponseDto create(PatternRequestDto dto) {
        return createOrUpdate(dto, new Pattern());
    }

    @Override
    public PatternResponseDto update(PatternRequestDto dto) {
        Pattern pattern = getPatternIfExists(dto.getId());
        return createOrUpdate(dto, pattern);
    }

    @Override
    public void delete(Long id) {
        getPatternIfExists(id);
        patternRepository.deleteById(id);
    }


    // TODO: move this method to a better place (make SET)

    @Override
    public List<Skill> getAllSkillsIfSkillIdsExist(List<Long> skillList) {
        return getAllSkillsIfSkillIdsExist(skillList, false);
    }
    @Override
    public List<Skill> getAllAssignableSkillsIfSkillIdsExist(List<Long> skillList) {
        return getAllSkillsIfSkillIdsExist(skillList, true);
    }

    @Override
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
                throw new AlreadyExistsException("Pattern with name: '" + name + "' already exists.", "name");
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

    private PatternResponseDto createOrUpdate(PatternRequestDto dto, Pattern pattern) {
        String newName = dto.getName();
        checkIfPatternNameAlreadyExistsForAnotherID(newName, dto.getId());

        pattern.setName(newName);
        pattern.setParent(getParentIfExists(dto.getParentId()));
        pattern.setSuggestedSkills(getAllSkillsIfSkillIdsExist(dto.getSuggestedSkills()));
        return PatternMapper.toDto(patternRepository.save(pattern));
    }
}
