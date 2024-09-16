package com.example.b2b_opportunities.Static;

import lombok.Getter;

@Getter
public enum WorkLocation {
    OFFICE(1),
    HYBRID(2),
    REMOTE(3);

    private final long id;

    WorkLocation(long id) {
        this.id = id;
    }
}
