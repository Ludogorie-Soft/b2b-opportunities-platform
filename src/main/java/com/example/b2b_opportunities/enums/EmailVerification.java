package com.example.b2b_opportunities.enums;

import lombok.Getter;

@Getter
public enum EmailVerification {
    ACCEPTED(1),
    PENDING(2);

    private final long id;

    EmailVerification(long id) {
        this.id = id;
    }
}