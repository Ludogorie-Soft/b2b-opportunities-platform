package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Entity.Currency;

public interface CurrencyService {
    public Currency getById(Long id);

    public Currency create(String name);

    public Currency edit(Long id, String newName);

    public void deleteById(Long id);
}
