package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.entity.Currency;
import com.example.b2b_opportunities.exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.repository.CurrencyRepository;
import com.example.b2b_opportunities.services.impl.CurrencyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {
    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyServiceImpl currencyService;

    @Test
    void shouldCreateCurrencyWhenDoesNotExist() {
        String currencyName = "TEST";
        Currency savedCurrency = Currency.builder().name(currencyName).build();

        when(currencyRepository.findByName(any(String.class))).thenReturn(Optional.empty());
        when(currencyRepository.save(any(Currency.class))).thenReturn(savedCurrency);

        Currency result = currencyService.create(currencyName);

        assertEquals(currencyName, result.getName());
        verify(currencyRepository, times(1)).findByName(currencyName);
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    void shouldNotCreateCurrencyWhenCurrencyExists() {
        String currencyName = "TEST";
        Currency existingCurrency = Currency.builder().name(currencyName).build();

        when(currencyRepository.findByName(any(String.class))).thenReturn(Optional.of(existingCurrency));

        AlreadyExistsException existsException = assertThrows(AlreadyExistsException.class, () -> currencyService.create(currencyName));

        verify(currencyRepository, times(1)).findByName(currencyName);

        assertEquals("Currency with name: TEST already exists", existsException.getMessage());
    }

    @Test
    void shouldEditCurrencyWhenExists() {
        String currencyName = "existing";
        Currency existingCurrency = Currency.builder().id(99999L).name(currencyName).build();
        String newName = "new-name";

        when(currencyRepository.findById(any())).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.save(any(Currency.class))).thenReturn(existingCurrency);

        Currency result = currencyService.edit(99999L, newName);

        verify(currencyRepository, times(1)).findByName(newName);
        verify(currencyRepository, times(1)).findById(99999L);
        verify(currencyRepository, times(1)).save(existingCurrency);
        assertEquals(result.getName(), newName);
    }

    @Test
    void shouldNotEditCurrencyWhenNameAlreadyExists() {
        String existingName = "existing";
        String newName = "existing";

        Currency currencyToEdit = Currency.builder().id(99999L).name("old-name").build();
        Currency conflictingCurrency = Currency.builder().id(88888L).name(existingName).build();

        when(currencyRepository.findById(99999L)).thenReturn(Optional.of(currencyToEdit));
        when(currencyRepository.findByName(newName)).thenReturn(Optional.of(conflictingCurrency));

        AlreadyExistsException existsException = assertThrows(AlreadyExistsException.class, () -> currencyService.edit(99999L, newName));

        verify(currencyRepository, times(1)).findById(99999L);
        verify(currencyRepository, times(1)).findByName(newName);
        verify(currencyRepository, never()).save(any(Currency.class));

        assertEquals("Currency with name: existing already exists", existsException.getMessage());
    }

    @Test
    void shouldDeleteWhenCurrencyExists(){
        Currency existingCurrency = Currency.builder().id(99999L).name("currency-to-be-deleted").build();
        when(currencyRepository.findById(99999L)).thenReturn(Optional.of(existingCurrency));

        currencyService.deleteById(99999L);

        verify(currencyRepository, times(1)).findById(99999L);
        verify(currencyRepository, times(1)).delete(existingCurrency);
    }

}