package com.example.b2b_opportunities.Exception;

import com.example.b2b_opportunities.Exception.common.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PasswordsNotMatchingException extends BaseException {
    public PasswordsNotMatchingException(String message) {
        super(message);
    }
    public PasswordsNotMatchingException(String message, String field) {
        super(message, field);
    }

}
