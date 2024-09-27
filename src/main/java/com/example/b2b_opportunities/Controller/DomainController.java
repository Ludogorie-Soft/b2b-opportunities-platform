package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Exception.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/domains")
@RequiredArgsConstructor
public class DomainController {
    private final DomainRepository domainRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Domain> getDomains() {
        return domainRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Domain addDomain(@RequestParam String name) {
        checkIfAlreadyExists(name);
        return domainRepository.save(Domain.builder().name(name).build());
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Domain editDomain(@RequestParam String name, @RequestParam String newName) {
        Domain domain = findByName(name);
        checkIfAlreadyExists(newName);
        domain.setName(newName);
        return domainRepository.save(domain);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDomain(@RequestParam(required = false) String name) {
        Domain domain = findByName(name);
        domainRepository.delete(domain);
    }

    private void checkIfAlreadyExists(String name) {
        if (domainRepository.findByName(name).isPresent()) {
            throw new AlreadyExistsException("Domain already exists");
        }
    }

    private Domain findByName(String name) {
        return domainRepository.findByName(name).orElseThrow(() -> new NotFoundException("Domain with name " + name + " not found"));
    }
}
