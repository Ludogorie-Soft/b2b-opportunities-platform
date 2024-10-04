package com.example.b2b_opportunities.Static;

import lombok.Getter;

@Getter
public enum WorkMode {
    OFFICE(1),
    HYBRID(2),
    REMOTE(3);

    private final long id;

    WorkMode(long id) {
        this.id = id;
    }
}
