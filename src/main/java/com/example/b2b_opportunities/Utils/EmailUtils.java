package com.example.b2b_opportunities.Utils;

import com.example.b2b_opportunities.Exception.common.InvalidRequestException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EmailUtils {

    public static void validateEmail(String email) {
        if (!isValidEmail(email) || !isValidDomain(email))
            throw new InvalidRequestException("Invalid email format or domain.");
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}$"); // Basic regex for email validation
    }

    private static boolean isValidDomain(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        try {
            InetAddress.getByName(domain); // Checks if the domain is resolvable
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
