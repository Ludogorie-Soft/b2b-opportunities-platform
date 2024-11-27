package com.example.b2b_opportunities.Utils;

import com.example.b2b_opportunities.Exception.common.InvalidRequestException;

public class StringUtils {
    public static String stripCapitalizeAndValidateNotEmpty(String name) {
        return stripCapitalizeAndValidateNotEmpty(name, name);
    }

    public static String stripCapitalizeAndValidateNotEmpty(String name, String context) {
        validateNameNotBlank(name, context);
        return capitalizeWords(name.strip());
    }

    private static void validateNameNotBlank(String name, String context) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException(context.strip() + " cannot be null or blank", "name");
        }
    }

    private static String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private static String capitalizeWords(String input) {
        String[] words = input.split("\\s+"); // Split by one or more spaces
        StringBuilder capitalizedWords = new StringBuilder();

        for (String word : words) {
            capitalizedWords
                    .append(capitalize(word))
                    .append(" ");
        }

        return capitalizedWords.toString().strip();
    }
}
