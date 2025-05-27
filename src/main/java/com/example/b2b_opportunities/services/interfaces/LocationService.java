package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.entity.Location;

import java.util.List;

public interface LocationService {
    Location get(Long id);

    List<Location> getAll();

    Location create(String name);

    Location update(Long id, String newName);

    void delete(Long id);
}
