package com.example.b2b_opportunities.enums;

import lombok.Getter;

@Getter
public enum ProjectStatus {
    ACTIVE(1),
    INACTIVE(2);

    private final long id;

    ProjectStatus(long id) {
        this.id = id;
    }
}
