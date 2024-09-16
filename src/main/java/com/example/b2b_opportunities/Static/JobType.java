package com.example.b2b_opportunities.Static;

import lombok.Getter;

@Getter
public enum JobType {
    FULL_TIME(1),
    PART_TIME(2);

    private final long id;

    JobType(long id) {
        this.id = id;
    }
}
