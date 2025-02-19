<<<<<<<< HEAD:src/main/java/com/example/b2b_opportunities/Service/Impl/CurrencyServiceImpl.java
package com.example.b2b_opportunities.Service.Impl;
========
package com.example.b2b_opportunities.Service.Implementation;
>>>>>>>> 6778bc4 (- Create interfaces and move the old service logic in implementation class):src/main/java/com/example/b2b_opportunities/Service/Implementation/CurrencyServiceImpl.java

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
