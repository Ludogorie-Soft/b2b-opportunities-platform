package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.ProjectRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Dto.Response.ProjectResponseDto;
import com.example.b2b_opportunities.Service.Interface.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ProjectResponseDto get(Authentication authentication, @PathVariable("id") Long id) {
        return projectService.get(authentication, id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<ProjectResponseDto> getAvailableProjects(Authentication authentication,
                                                         @RequestParam(defaultValue = "0") int offset,
                                                         @RequestParam(defaultValue = "10")int pageSize,
                                                         @RequestParam String sort,
                                                         @RequestParam boolean ascending) {
        return projectService.getAvailableProjects(authentication, offset, pageSize, sort, ascending);
    }

    @GetMapping("{id}/positions")
    @ResponseStatus(HttpStatus.OK)
    public List<PositionResponseDto> getPositionsByProject(Authentication authentication, @PathVariable("id") Long id) {
        return projectService.getPositionsByProject(authentication, id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto update(@PathVariable("id") Long id, @RequestBody ProjectRequestDto dto, Authentication authentication) {
        return projectService.update(id, dto, authentication);
    }

    @GetMapping("/{id}/reactivate")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto activate(@PathVariable("id") Long id, Authentication authentication) {
        return projectService.reactivateProject(id, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDto create(Authentication authentication,
                                     @RequestBody @Valid ProjectRequestDto projectRequestDto) {
        return projectService.create(authentication, projectRequestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id, Authentication authentication) {
        projectService.delete(id, authentication);
    }
}
