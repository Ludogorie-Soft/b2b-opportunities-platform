package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Entity.Location;

import java.util.List;

public interface LocationService {
    Location get(Long id);

    List<Location> getAll();

    Location create(String name);

    Location update(Long id, String newName);

    void delete(Long id);
}
