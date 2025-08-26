package com.example.b2b_opportunities.entity;

import lombok.Getter;

@Getter
public enum CompanyRole {
    COMPANY_ADMIN(1),
    COMPANY_USER(2);

    private final long id;

    CompanyRole(long id) {
        this.id = id;
    }
}