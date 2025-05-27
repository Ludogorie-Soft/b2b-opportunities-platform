package com.example.b2b_opportunities.utils;

import com.example.b2b_opportunities.exception.common.InvalidRequestException;

import java.util.regex.Pattern;

public class EmailUtils {

    private static final int MAX_EMAIL_LENGTH = 320;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);


    public static void validateEmail(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_EMAIL_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidRequestException("Invalid email format or domain.", "email");
        }
    }
}
