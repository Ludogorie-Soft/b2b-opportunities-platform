package com.example.b2b_opportunities.Utils;

import com.example.b2b_opportunities.Exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import static com.example.b2b_opportunities.Utils.StringUtils.stripCapitalizeAndValidateNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringUtilsTest {
    private final String context = "context";

    @Test
    void throwErrorWhenNameIsNull() {
        String name = null;
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            stripCapitalizeAndValidateNotEmpty(name, context);
        });
    }

    @Test
    void throwErrorWhenNameIsEmpty() {
        String name = "";
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            stripCapitalizeAndValidateNotEmpty(name, context);
        });
    }

    @Test
    void throwErrorWhenNameIsBlank() {
        String name = "   ";
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            stripCapitalizeAndValidateNotEmpty(name, context);
        });
    }

    @Test
    void shouldStripTheContextWithSingleWord() {
        String name = "   ";
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            stripCapitalizeAndValidateNotEmpty(name, "  word  ");
        });
        assertEquals(exception.getMessage(), "word cannot be null or blank");
    }

    @Test
    void shouldStripTheContextWithMultipleWordsAndKeepTheCapitalizationAndSpacesBetweenWords() {
        String name = "   ";
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            stripCapitalizeAndValidateNotEmpty(name, "  One   twO  ");
        });
        assertEquals(exception.getMessage(), "One   twO cannot be null or blank");
    }

    @Test
    void shouldCapitalizeSingleWord() {
        String name = "word";
        String result = stripCapitalizeAndValidateNotEmpty(name, context);
        assertEquals("Word", result);
    }

    @Test
    void shouldCapitalizeTwoWords() {
        String name = "one two";
        String result = stripCapitalizeAndValidateNotEmpty(name, context);
        assertEquals("One Two", result);
    }

    @Test
    void shouldCapitalizeThreeWords() {
        String name = "one two three";
        String result = stripCapitalizeAndValidateNotEmpty(name, context);
        assertEquals("One Two Three", result);
    }

    @Test
    void shouldCapitalizeAndStripThreeWordsWithMultipleSpaces() {
        String name = "   one    two   three    ";
        String result = stripCapitalizeAndValidateNotEmpty(name, context);
        assertEquals("One Two Three", result);
    }

    @Test
    void shouldCapitalizeAndStripSingleWordWithMultipleSpacesAndSmallBigLettersMixed() {
        String name = "   thIsIsAFunnyWord   ";
        String result = stripCapitalizeAndValidateNotEmpty(name, context);
        assertEquals("Thisisafunnyword", result);
    }
}