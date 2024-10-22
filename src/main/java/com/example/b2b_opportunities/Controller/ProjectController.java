package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.ProjectEditRequestDto;
import com.example.b2b_opportunities.Dto.Request.ProjectRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto get(@PathVariable("id") Long id) {
        return projectService.get(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectResponseDto> getAll(Authentication authentication) {
        return projectService.getAvailableProjects(authentication);
    }

    @GetMapping("{id}/positions")
    @ResponseStatus(HttpStatus.OK)
    public List<PositionResponseDto> getPositionsByProject(@PathVariable("id") Long id) {
        //TODO - add authentication and check if project is visible to current user (if shared ony w/ partners)
        return projectService.getPositionsByProject(id);  // field boolean isPrivate = false?
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto update(@PathVariable("id") Long id, @RequestBody ProjectEditRequestDto dto, Authentication authentication) {
        return projectService.update(id, dto, authentication);
    }

    @GetMapping("/{id}/reactivate")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto activate(@PathVariable("id") Long id, Authentication authentication) {
        return projectService.reactivateProject(id, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDto create(@RequestBody @Valid ProjectRequestDto projectRequestDto) {
        return projectService.create(projectRequestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id, Authentication authentication) {
        projectService.delete(id, authentication);
    }
}
