package com.example.b2b_opportunities.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SeniorityNotFoundException extends RuntimeException {
    public SeniorityNotFoundException(String message) {
        super(message);
    }
}