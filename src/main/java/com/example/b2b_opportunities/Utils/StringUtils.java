package com.example.b2b_opportunities.Utils;

public class StringUtils {
    public static String validateAndTrimName(String name, String context) {
        String trimmedName = name.trim();
        validateNameNotBlank(trimmedName, context);
        return trimmedName;
    }

    private static void validateNameNotBlank(String name, String context) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(context + " cannot be blank");
        }
    }
}
