package com.example.b2b_opportunities.enums;

import lombok.Getter;

@Getter
public enum ApplicationStatus {

    IN_PROGRESS(1),
    DENIED(2),
    ACCEPTED(3),
    AWAITING_CV_OR_TALENT(4),
    CANCELLED(5);

    private final long id;

    ApplicationStatus(long id) {
        this.id = id;
    }
}
