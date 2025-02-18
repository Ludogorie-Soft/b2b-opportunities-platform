package com.example.b2b_opportunities.Service.Impl;

import com.example.b2b_opportunities.Entity.Currency;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Repository.CurrencyRepository;
import com.example.b2b_opportunities.Service.Interface.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyRepository currencyRepository;

    public Currency getById(Long id) {
        return getCurrencyOrThrow(id);
    }

    public Currency create(String name) {
        checkIfAlreadyExists(name);
        return currencyRepository.save(Currency.builder().name(name).build());
    }

    public Currency edit(Long id, String newName) {
        Currency currency = getCurrencyOrThrow(id);
        if (!Objects.equals(newName, currency.getName())) {
            checkIfAlreadyExists(newName);
            currency.setName(newName);
        }
        return currencyRepository.save(currency);
    }

    public void deleteById(Long id) {
        currencyRepository.delete(getCurrencyOrThrow(id));
    }

    private void checkIfAlreadyExists(String currencyName) {
        if (currencyRepository.findByName(currencyName).isPresent()) {
            throw new AlreadyExistsException("Currency with name: " + currencyName + " already exists", "name");
        }
    }

    private Currency getCurrencyOrThrow(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Currency with ID: " + id + " not found"));
    }
}
