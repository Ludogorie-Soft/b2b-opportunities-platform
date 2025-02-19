package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Entity.Currency;

public interface CurrencyService {
    Currency getById(Long id);

    Currency create(String name);

    Currency edit(Long id, String newName);

    void deleteById(Long id);
}
