package com.example.b2b_opportunities.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class DisabledEmployerException extends RuntimeException{
    public DisabledEmployerException(String message) {
        super(message);
    }
}
