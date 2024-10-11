package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.PatternRequestDto;
import com.example.b2b_opportunities.Dto.Response.PatternResponseDto;
import com.example.b2b_opportunities.Entity.Pattern;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Repository.PatternRepository;
import com.example.b2b_opportunities.Repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PatternServiceTest {

    @InjectMocks
    private PatternService patternService;

    @Mock
    private PatternRepository patternRepository;

    @Mock
    private SkillRepository skillRepository;

    private Pattern pattern;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        pattern = new Pattern();
        pattern.setName("test");
        pattern.setId(99999L);
        pattern.setSuggestedSkills(new ArrayList<>());
    }

    @Test
    public void shouldReturnPattern(){
        when(patternRepository.findById(99999L)).thenReturn(Optional.of(pattern));

        PatternResponseDto result = patternService.get(99999L);

        assertEquals(result.getName(),"test");
        assertEquals(result.getId(), 99999L);
        assertNotNull(result);
    }

    @Test
    public void shouldThrowExceptionWhenPatternNotFound(){
        when(patternRepository.findById(99999999L)).thenReturn(Optional.empty());

        NotFoundException expectedException = assertThrows(NotFoundException.class, () -> {
            patternService.get(99999999L);
        });

        assertEquals("Pattern with ID: " + 99999999 + " not found", expectedException.getMessage());
        verify(patternRepository).findById(99999999L);
    }

    @Test
    public void testGetAll_ReturnsPatternResponseDtoList() {
        Pattern pattern1 = new Pattern();
        pattern1.setId(1L);
        pattern1.setName("Pattern 1");
        pattern1.setSuggestedSkills(new ArrayList<>());

        Pattern pattern2 = new Pattern();
        pattern2.setId(2L);
        pattern2.setName("Pattern 2");
        pattern2.setSuggestedSkills(new ArrayList<>());

        List<Pattern> patterns = Arrays.asList(pattern1, pattern2);

        when(patternRepository.findAll()).thenReturn(patterns);

        List<PatternResponseDto> result = patternService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        PatternResponseDto expectedDto1 = new PatternResponseDto();
        expectedDto1.setId(1L);
        expectedDto1.setName("Pattern 1");

        PatternResponseDto expectedDto2 = new PatternResponseDto();
        expectedDto2.setId(2L);
        expectedDto2.setName("Pattern 2");

        assertEquals(expectedDto1.getName(), result.get(0).getName());
        assertEquals(expectedDto2.getName(), result.get(1).getName());

        verify(patternRepository, times(1)).findAll();
    }

    @Test
    public void testCreatePattern(){
        PatternRequestDto dto = new PatternRequestDto();
        dto.setName("test");
        dto.setSuggestedSkills(new ArrayList<>());

        Pattern savedPattern = new Pattern();
        savedPattern.setId(1L);
        savedPattern.setName(dto.getName());
        savedPattern.setSuggestedSkills(new ArrayList<>());

        when(patternRepository.findByName("test")).thenReturn(Optional.empty());
        when(patternRepository.save(any(Pattern.class))).thenReturn(savedPattern);

        PatternResponseDto result = patternService.create(dto);

        verify(patternRepository, times(1)).findByName("test");
        verify(patternRepository, times(1)).save(any(Pattern.class));

        assertNotNull(result);
        assertEquals("test", result.getName());
        assertEquals(dto.getSuggestedSkills(), result.getSuggestedSkills());
    }

    @Test
    public void testUpdatePattern(){
        PatternRequestDto dto = new PatternRequestDto();
        dto.setId(1L);
        dto.setName("newName");
        dto.setSuggestedSkills(new ArrayList<>());

        Pattern existingPattern = new Pattern();
        existingPattern.setId(1L);
        existingPattern.setName("oldName");
        existingPattern.setSuggestedSkills(new ArrayList<>());

        when(patternRepository.findById(1L)).thenReturn(Optional.of(existingPattern));
        when(patternRepository.save(any(Pattern.class))).thenReturn(existingPattern);

        PatternResponseDto result = patternService.update(dto);

        verify(patternRepository, times(1)).findById(1L);
        verify(patternRepository, times(1)).save(any(Pattern.class));

        assertNotNull(result);
        assertEquals("newName", result.getName());
        assertEquals(dto.getSuggestedSkills(), result.getSuggestedSkills());
    }

    @Test
    public void testDelete_Success() {
        Long patternId = 1L;

        Pattern pattern = new Pattern();
        pattern.setId(patternId);

        when(patternRepository.findById(patternId)).thenReturn(Optional.of(pattern));

        patternService.delete(patternId);

        verify(patternRepository, times(1)).deleteById(patternId);
    }

    @Test
    public void testDelete_NotFound() {
        Long patternId = 1L;

        when(patternRepository.findById(patternId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> patternService.delete(patternId));

        verify(patternRepository, never()).deleteById(patternId);
    }

    @Test
    public void testCreate_WithNonExistentSkills() {
        PatternRequestDto dto = new PatternRequestDto();
        dto.setName("Test Pattern");
        dto.setSuggestedSkills(Arrays.asList(1L, 2L, 3L));

        Skill existingSkill = new Skill();
        existingSkill.setId(1L);

        when(skillRepository.findAllById(Arrays.asList(1L, 2L, 3L)))
                .thenReturn(List.of(existingSkill));

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            patternService.create(dto);
        });

        assertEquals("Skills with ID(s) [2, 3] not found.", thrown.getMessage());
        verify(patternRepository, never()).save(any(Pattern.class));
    }

    @Test
    public void testUpdate_WithNonExistentSkills() {
        Long patternId = 1L;
        PatternRequestDto dto = new PatternRequestDto();
        dto.setId(patternId);
        dto.setName("Updated Pattern");
        dto.setSuggestedSkills(Arrays.asList(1L, 2L, 3L));

        Pattern existingPattern = new Pattern();
        existingPattern.setId(patternId);
        existingPattern.setName("Old Pattern");

        when(patternRepository.findById(patternId)).thenReturn(Optional.of(existingPattern));

        Skill existingSkill = new Skill();
        existingSkill.setId(1L);

        when(skillRepository.findAllById(Arrays.asList(1L, 2L, 3L)))
                .thenReturn(List.of(existingSkill));

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> patternService.update(dto));

        assertEquals("Skills with ID(s) [2, 3] not found.", thrown.getMessage());
        verify(patternRepository, times(1)).findById(patternId);
        verify(patternRepository, never()).save(any(Pattern.class));
    }

}
