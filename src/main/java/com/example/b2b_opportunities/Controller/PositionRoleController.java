package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.PositionRole;
import com.example.b2b_opportunities.Exception.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Repository.PositionRoleRepository;
import com.example.b2b_opportunities.Utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class PositionRoleController {
    private final PositionRoleRepository positionRoleRepository;
    private final DomainController domainController;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PositionRole> getAll() {
        return positionRoleRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionRole get(@PathVariable Long id) {
        return findPositionRoleByIdOrThrow(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PositionRole add(@RequestParam("name") String name) {
        name = StringUtils.stripCapitalizeAndValidateNotEmpty(name, "Role");
        validateNameDoesNotExist(name);
        return positionRoleRepository.save(PositionRole.builder().name(name).build());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PositionRole edit(@PathVariable("id") Long id, @RequestParam("newName") String newName) {
        PositionRole positionRole = findPositionRoleByIdOrThrow(id);

        newName = StringUtils.stripCapitalizeAndValidateNotEmpty(newName, "Role");
        if (Objects.equals(positionRole.getName(), newName)) {
            return positionRole;
        }
        validateNameDoesNotExist(newName);

        positionRole.setName(newName);
        return positionRoleRepository.save(positionRole);
    }

    private PositionRole findPositionRoleByIdOrThrow(Long id) {
        return positionRoleRepository.findById(id).orElseThrow(() -> new NotFoundException("Role with ID: " + id + " not found"));
    }

    private void validateNameDoesNotExist(String name) {
        if (positionRoleRepository.findByName(name).isPresent()) {
            throw new AlreadyExistsException("Role with name: '" + name + "' already exists");
        }
    }
}
