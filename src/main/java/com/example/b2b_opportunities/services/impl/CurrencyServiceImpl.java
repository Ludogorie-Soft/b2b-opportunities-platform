package com.example.b2b_opportunities.services.impl;

import com.example.b2b_opportunities.entity.Currency;
import com.example.b2b_opportunities.exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.exception.common.NotFoundException;
import com.example.b2b_opportunities.repository.CurrencyRepository;
import com.example.b2b_opportunities.services.interfaces.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyRepository currencyRepository;

    @Override
    public Currency getById(Long id) {
        return getCurrencyOrThrow(id);
    }

    @Override
    public Currency create(String name) {
        checkIfAlreadyExists(name);
        return currencyRepository.save(Currency.builder().name(name).build());
    }

    @Override
    public Currency edit(Long id, String newName) {
        Currency currency = getCurrencyOrThrow(id);
        if (!Objects.equals(newName, currency.getName())) {
            checkIfAlreadyExists(newName);
            currency.setName(newName);
        }
        return currencyRepository.save(currency);
    }

    @Override
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