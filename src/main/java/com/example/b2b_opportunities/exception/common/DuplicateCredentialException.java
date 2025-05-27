package com.example.b2b_opportunities.exception.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCredentialException extends BaseException {
    public DuplicateCredentialException(String message) {
        super(message);
    }


    public DuplicateCredentialException(String message, String field) {
        super(message, field);
    }
}
