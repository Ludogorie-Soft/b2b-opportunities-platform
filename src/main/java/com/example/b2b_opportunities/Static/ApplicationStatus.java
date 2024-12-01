package com.example.b2b_opportunities.Static;

import lombok.Getter;

@Getter
public enum ApplicationStatus {

    IN_PROGRESS(1),
    DENIED(2);

    private final long id;

    ApplicationStatus(long id) {
        this.id = id;
    }
}
