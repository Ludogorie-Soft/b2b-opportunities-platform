package com.example.b2b_opportunities.Utils;

import com.example.b2b_opportunities.Exception.common.InvalidRequestException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class EmailUtils {

    private static final int MAX_EMAIL_LENGTH = 320;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}$");


    public static void validateEmail(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_EMAIL_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidRequestException("Invalid email format or domain.", "email");
        }
    }
}
