package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Exception.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.InvalidInputException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
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
@RequestMapping("/company-types")
@RequiredArgsConstructor
public class CompanyTypeController {
    private final CompanyTypeRepository companyTypeRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompanyType> getCompanyTypes() {
        return companyTypeRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyType get(@PathVariable Long id) {
        return findCompanyTypeByIdOrThrow(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyType addCompanyType(@RequestParam("name") String name) {
        name = validateAndTrimName(name);
        validateNameDoesNotExists(name);
        return companyTypeRepository.save(CompanyType.builder().name(name).build());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyType editCompanyType(@PathVariable Long id, @RequestParam("newName") String newName) {
        CompanyType companyType = findCompanyTypeByIdOrThrow(id);

        newName = validateAndTrimName(newName);
        if (Objects.equals(companyType.getName(), newName)) {
            return companyType;
        }
        validateNameDoesNotExists(newName);

        companyType.setName(newName);
        return companyTypeRepository.save(companyType);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompanyType(@PathVariable Long id) {
        companyTypeRepository.delete(findCompanyTypeByIdOrThrow(id));
    }

    private void validateNameDoesNotExists(String name) {
        if (companyTypeRepository.findByName(name).isPresent())
            throw new AlreadyExistsException("Company type with name: '" + name + "'  already exists");
    }

    private CompanyType findCompanyTypeByIdOrThrow(Long id) {
        return companyTypeRepository.findById(id).orElseThrow(() -> new NotFoundException("Company type with ID: " + id + " not found"));
    }

    private void validateNameNotBlank(String name) {
        if (name.isBlank()) {
            throw new InvalidInputException("Company type name cannot be blank");
        }
    }

    private String validateAndTrimName(String name) {
        String trimmedName = name.trim();
        validateNameNotBlank(trimmedName);
        return trimmedName;
    }
}