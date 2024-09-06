package com.example.b2b_opportunities.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailInUseException extends RuntimeException{
    public EmailInUseException(String message) {
        super(message);
    }
}
