package com.example.b2b_opportunities.Utils;

import com.example.b2b_opportunities.Exception.common.InvalidRequestException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class EmailUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}$");


    public static void validateEmail(String email) {
        if (!isValidEmail(email))
            throw new InvalidRequestException("Invalid email format or domain.", "email");
    }

    private static boolean isValidEmail(String email) {
        if (email == null || email.length() > 320) { // Basic check for null or unreasonably long emails
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
