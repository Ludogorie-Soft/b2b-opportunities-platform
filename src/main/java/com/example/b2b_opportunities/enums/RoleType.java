package com.example.b2b_opportunities.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    ROLE_ADMIN(1),
    ROLE_USER(2);

    private final long id;

    RoleType(long id) {
        this.id = id;
    }
}
