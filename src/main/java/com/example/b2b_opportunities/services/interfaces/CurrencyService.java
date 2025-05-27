package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.entity.Currency;

public interface CurrencyService {
    Currency getById(Long id);

    Currency create(String name);

    Currency edit(Long id, String newName);

    void deleteById(Long id);
}
