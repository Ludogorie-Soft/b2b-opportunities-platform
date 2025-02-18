package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Entity.Location;

import java.util.List;

import static com.example.b2b_opportunities.Utils.StringUtils.stripCapitalizeAndValidateNotEmpty;

public interface LocationService {

    public Location get(Long id) ;

    public List<Location> getAll();

    public Location create(String name);

    public Location update(Long id, String newName);

    public void delete(Long id);
}
