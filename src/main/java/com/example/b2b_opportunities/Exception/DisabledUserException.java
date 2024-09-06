package com.example.b2b_opportunities.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class DisabledUserException extends RuntimeException{
    public DisabledUserException(String message) {
        super(message);
    }
}
