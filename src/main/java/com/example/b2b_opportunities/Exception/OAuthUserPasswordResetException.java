package com.example.b2b_opportunities.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class OAuthUserPasswordResetException extends RuntimeException{
    public OAuthUserPasswordResetException(String message) {
        super(message);
    }
}
