package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Exception.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.InvalidInputException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/domains")
@RequiredArgsConstructor
public class DomainController {
    private final DomainRepository domainRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Domain> getAll() {
        return domainRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Domain get(@PathVariable Long id) {
        return findDomainByIdOrThrow(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Domain add(@RequestParam("name") String name) {
        name = validateAndTrimName(name);
        validateNameDoesNotExists(name);
        return domainRepository.save(Domain.builder().name(name).build());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Domain edit(@PathVariable Long id, @RequestParam("newName") String newName) {
        Domain domain = findDomainByIdOrThrow(id);

        newName = validateAndTrimName(newName);
        if (Objects.equals(domain.getName(), newName)) {
            return domain;
        }
        validateNameDoesNotExists(newName);

        domain.setName(newName);
        return domainRepository.save(domain);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        domainRepository.delete(findDomainByIdOrThrow(id));
    }

    private void validateNameDoesNotExists(String name) {
        if (domainRepository.findByName(name).isPresent()) {
            throw new AlreadyExistsException("Domain with name: '" + name + "' already exists");
        }
    }

    private Domain findDomainByIdOrThrow(Long id) {
        return domainRepository.findById(id).orElseThrow(() -> new NotFoundException("Domain with ID: " + id + " not found"));
    }

    private void validateNameNotBlank(String name) {
        if (name.isBlank()) {
            throw new InvalidInputException("Domain name cannot be blank");
        }
    }

    private String validateAndTrimName(String name) {
        String trimmedName = name.trim();
        validateNameNotBlank(trimmedName);
        return trimmedName;
    }
}
