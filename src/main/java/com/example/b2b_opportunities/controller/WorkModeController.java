package com.example.b2b_opportunities.controller;

import com.example.b2b_opportunities.dto.requestDtos.WorkmodeRequestDto;
import com.example.b2b_opportunities.entity.WorkMode;
import com.example.b2b_opportunities.exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.exception.common.NotFoundException;
import com.example.b2b_opportunities.repository.WorkModeRepository;
import com.example.b2b_opportunities.utils.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workmodes")
public class WorkModeController {
    private final WorkModeRepository workModeRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<WorkMode> getWorkModes() {
        return workModeRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WorkMode getWorkModeById(@PathVariable(name = "id") Long id) {
        return getWorkModeOrThrow(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addWorkMode(@RequestBody @Valid WorkmodeRequestDto dto) {
        validateNameNotExist(dto.getName());
        WorkMode workMode = new WorkMode();
        workMode.setName(StringUtils.stripCapitalizeAndValidateNotEmpty(dto.getName()));
        workModeRepository.save(workMode);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkMode(@PathVariable(name = "id") Long id) {
        getWorkModeOrThrow(id);
        workModeRepository.deleteById(id);
    }

    private WorkMode getWorkModeOrThrow(Long id) {
        return workModeRepository.findById(id).orElseThrow(
                () -> new NotFoundException("WorkMode with ID: " + id + " not found"));
    }

    private void validateNameNotExist(String newName) {
        List<WorkMode> workModes = workModeRepository.findAll();
        for (WorkMode w : workModes) {
            if (w.getName().equalsIgnoreCase(newName)) {
                throw new AlreadyExistsException("WorkMode with name: '" + StringUtils.stripCapitalizeAndValidateNotEmpty(newName) + "' already exists", "name");
            }
        }
    }
}
