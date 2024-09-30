package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.CompanyType;
import com.example.b2b_opportunities.Entity.Domain;
import com.example.b2b_opportunities.Exception.AlreadyExistsException;
import com.example.b2b_opportunities.Repository.CompanyTypeRepository;
import lombok.Getter;
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
@RequestMapping("/company-type")
@RequiredArgsConstructor
public class CompanyTypeController {

    private final CompanyTypeRepository companyTypeRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompanyType> getCompanyTypes(){
        return companyTypeRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyType addCompanyType(@RequestParam String name){
        checkIfAlreadyExists(name);
        return companyTypeRepository.save(CompanyType.builder().name(name).build());
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public CompanyType editCompanyType(@RequestParam String name, @RequestParam String newName) {
        CompanyType companyType = findByName(name);
        checkIfAlreadyExists(newName);
        companyType.setName(newName);
        return companyTypeRepository.save(companyType);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompanyType(@RequestParam(required = false) String name) {
        CompanyType companyType = findByName(name);
        companyTypeRepository.delete(companyType);
    }

    private void checkIfAlreadyExists(String name){
        if(companyTypeRepository.findByName(name).isPresent())
            throw new AlreadyExistsException("Company type already exists");
    }

    private CompanyType findByName(String name){
        return companyTypeRepository.findByName(name).orElseThrow(() -> new AlreadyExistsException("Company type with name " + name + " not found"));
    }


}
