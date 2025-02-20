package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.Currency;
import com.example.b2b_opportunities.Repository.CurrencyRepository;
import com.example.b2b_opportunities.Service.Implementation.CurrencyServiceImpl;
import com.example.b2b_opportunities.Service.Interface.CurrencyService;
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

@RestController
@RequestMapping("/currencies")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyRepository currencyRepository;
    private final CurrencyService currencyService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Currency getById(@PathVariable("id") Long id) {
        return currencyService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Currency create(@RequestParam String name) {
        return currencyService.create(name);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Currency edit(@PathVariable("id") Long id, @RequestParam String name) {
        return currencyService.edit(id, name);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) {
        currencyService.deleteById(id);
    }
}
