package com.example.b2b_opportunities.Exception.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCredentialException extends RuntimeException{
    public DuplicateCredentialException(String message) {
        super(message);
    }
}
