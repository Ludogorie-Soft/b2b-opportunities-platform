<<<<<<<< HEAD:src/main/java/com/example/b2b_opportunities/Service/Impl/LocationServiceImpl.java
package com.example.b2b_opportunities.Service.Impl;
========
package com.example.b2b_opportunities.Service.Implementation;
>>>>>>>> 6778bc4 (- Create interfaces and move the old service logic in implementation class):src/main/java/com/example/b2b_opportunities/Service/Implementation/LocationServiceImpl.java

import com.example.b2b_opportunities.Entity.Location;
import com.example.b2b_opportunities.Exception.common.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Repository.LocationRepository;
import com.example.b2b_opportunities.Service.Interface.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.example.b2b_opportunities.Utils.StringUtils.stripCapitalizeAndValidateNotEmpty;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    @Override
    public Location get(Long id) {
        return getLocationsIfExists(id);
    }

    @Override
    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    @Override
    public Location create(String name) {
        name = stripCapitalizeAndValidateNotEmpty(name, "Location Name");

        validateLocationNameDoesNotExist(name);
        Location location = new Location();
        location.setName(name);
        return locationRepository.save(location);
    }

    @Override
    public Location update(Long id, String newName) {
        newName = stripCapitalizeAndValidateNotEmpty(newName, "Location Name");

        Location location = getLocationsIfExists(id);
        if (!Objects.equals(location.getName(), newName)) {
            validateLocationNameDoesNotExist(newName);
            location.setName(newName);
            return locationRepository.save(location);
        }
        return location;
    }

    @Override
    public void delete(Long id) {
        getLocationsIfExists(id);
        locationRepository.deleteById(id);
    }
    
    private void validateLocationNameDoesNotExist(String name) {
        if (locationRepository.findByName(name).isPresent()) {
            throw new AlreadyExistsException("Location with name: '" + name + "' already exists", "name");
        }
    }

    private Location getLocationsIfExists(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new NotFoundException("Location with ID: " + id + " not found"));
    }
}
