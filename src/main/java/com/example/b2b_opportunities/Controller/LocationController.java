package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Entity.Location;
import com.example.b2b_opportunities.Service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Location get(@PathVariable("id") Long id) {
        return locationService.get(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Location> getAll() {
        return locationService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location create(@RequestParam(name = "name") String name) {
        return locationService.create(name);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Location update(@PathVariable("id") Long id, @RequestParam String newName) {
        return locationService.update(id, newName);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        locationService.delete(id);
    }
}
