package com.example.b2b_opportunities.Exception.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyExistsException extends BaseException {
    public AlreadyExistsException(String message) {
        super(message);
    }

    public AlreadyExistsException(String message, String field) {
        super(message, field);
    }
}
