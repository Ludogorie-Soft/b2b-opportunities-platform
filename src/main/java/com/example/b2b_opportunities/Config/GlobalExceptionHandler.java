package com.example.b2b_opportunities.Config;

import com.example.b2b_opportunities.Exception.ValidationException;
import com.example.b2b_opportunities.Exception.common.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.BindException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        HttpStatus status = getStatus(ex);
        logException(ex, request, status);
        return buildResponseEntity(ex, status, request);
    }

    private ResponseEntity<Map<String, Object>> buildResponseEntity(Exception ex, HttpStatus status, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", getMessage(ex));
        response.put("path", request.getRequestURI());

        String[] field = getFieldNamesFromException(ex);
        if (field != null) {
            response.put("field", field);
        }

        return new ResponseEntity<>(response, status);
    }


    private String[] getMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException ma) {
            return ma.getBindingResult().getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
        } else if (ex instanceof ValidationException ve) {
            return ve.getBindingResult().getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
        } else {
            return new String[]{ex.getMessage() != null ? ex.getMessage() : "An error occurred"};
        }
    }

    private HttpStatus getStatus(Exception ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        } else if (ex instanceof ValidationException || ex instanceof MethodArgumentNotValidException || ex instanceof BindException) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private void logException(Exception ex, HttpServletRequest request, HttpStatus status) {
        String logMessage = String.format("Exception caught: %s at path %s with status %s",
                ex.getClass().getSimpleName(), request.getRequestURI(), status);

        if (status.is4xxClientError()) {
            log.warn(logMessage);
        } else if (status.is5xxServerError()) {
            log.error(logMessage, ex);
        } else {
            log.info(logMessage);
        }
    }

    private String[] getFieldNameFromBindingResult(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(FieldError::getField)
                .toArray(String[]::new);
    }

    private String[] getFieldNamesFromException(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manve) {
            return getFieldNameFromBindingResult(manve.getBindingResult());
        } else if (ex instanceof ValidationException ve) {
            return getFieldNameFromBindingResult(ve.getBindingResult());
        } else if (ex instanceof BaseException baseException && baseException.getField() != null) {
            return new String[]{baseException.getField()};
        }
        return null;
    }
}