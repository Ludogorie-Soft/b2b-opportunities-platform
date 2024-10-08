package com.example.b2b_opportunities.Config;

import com.example.b2b_opportunities.Exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.BindException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        HttpStatus status = getStatus(ex);
        return buildResponseEntity(ex, status, request);
    }

    private ResponseEntity<Map<String, Object>> buildResponseEntity(Exception ex, HttpStatus status, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", getMessage(ex));
        response.put("path", request.getRequestURI());
        return new ResponseEntity<>(response, status);
    }

    private String getMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manve) {
            return manve.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        } else if (ex instanceof ValidationException validationException) {
            return validationException.getBindingResult().getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else {
            return ex.getMessage() != null ? ex.getMessage() : "An error occurred";
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
}