package com.example.b2b_opportunities.Exception.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends BaseException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, String field) {
        super(message, field);
    }
}