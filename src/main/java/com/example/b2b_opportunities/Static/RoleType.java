package com.example.b2b_opportunities.Static;

import lombok.Getter;

@Getter
public enum RoleType {
    ROLE_ADMIN(1),
    ROLE_EMPLOYER(2),
    ROLE_USER(3);

    private final long id;

    RoleType(long id) {
        this.id = id;
    }
}
