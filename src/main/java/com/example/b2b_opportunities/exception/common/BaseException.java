package com.example.b2b_opportunities.exception.common;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final String field;

    public BaseException(String message, String field) {
        super(message);
        this.field = field;
    }

    public BaseException(String message) {
        super(message);
        this.field = null;
    }
}
