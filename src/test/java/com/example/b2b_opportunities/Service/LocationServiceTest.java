package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.Location;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Repository.LocationRepository;
import com.example.b2b_opportunities.Service.Implementation.LocationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {
    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    @Test
    void shouldReturnLocationWhenExists() {
        Long id = 1L;
        String locationName = "testCity3";
        Location location = new Location();
        location.setId(id);
        location.setName(locationName);
        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        Location result = locationService.get(id);

        assertEquals(locationName, result.getName());
        assertEquals(id, result.getId());
        verify(locationRepository, times(1)).findById(id);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenLocationDoesNotExist() {
        Long id = 1L;
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            locationService.get(id);
        });

        assertEquals("Location with ID: 1 not found", thrown.getMessage());
        verify(locationRepository, times(1)).findById(id);  // Ensure findById was called once
    }

    @Test
    void shouldReturnEmptyList() {
        List<Location> emptyList = new ArrayList<>();
        when(locationRepository.findAll()).thenReturn(emptyList);

        List<Location> result = locationService.getAll();

        assertEquals(emptyList, result);
        verify(locationRepository, times(1)).findAll();
    }

    @Test
    void testCreateLocationSuccessfully() {
        String locationName = "New York";
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Location result = locationService.create(locationName);

        assertEquals("New York", result.getName());
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void shouldDeleteLocationWhenExists() {
        Long id = 1L;

        Location location = new Location();
        location.setId(id);
        location.setName("testCity3");

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        locationService.delete(id);

        verify(locationRepository, times(1)).findById(id);  // Ensure the location is checked for existence
        verify(locationRepository, times(1)).deleteById(id);  // Ensure the deleteById is called
    }


    @Test
    void shouldUpdateLocationWhenNameIsDifferent() {
        Long id = 1L;
        String newName = "Testcity2";

        Location location = new Location();
        location.setId(id);
        location.setName("Testcity3");

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenReturn(location);

        Location updatedLocation = locationService.update(id, newName);

        assertEquals(newName, updatedLocation.getName());
        verify(locationRepository, times(1)).findById(id);
        verify(locationRepository, times(1)).save(location);
    }

    @Test
    void shouldNotUpdateLocationWhenNameIsSame() {
        Long id = 1L;
        String sameName = "New York";
        Location location = new Location();
        location.setId(id);
        location.setName(sameName);

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        Location result = locationService.update(id, sameName);

        assertEquals(sameName, result.getName()); // Ensure the name remains the same
        verify(locationRepository, times(1)).findById(id); // Ensure the location is checked for existence
        verify(locationRepository, never()).save(any()); // Ensure save is never called
    }
}