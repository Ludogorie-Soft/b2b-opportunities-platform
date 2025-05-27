package com.example.b2b_opportunities.utils;

import com.example.b2b_opportunities.exception.common.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailUtilsTest {

    @InjectMocks
    EmailUtils emailUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @CsvSource({
            "test@test.com, valid",
            "test_test@test.com, valid",
            "nodomain.com, invalid",
            "randomtest, invalid",
            "emaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaillemaill, false"
    })
    void testEmailUtils(String email, String expectedOutcome) {
        if ("valid".equals(expectedOutcome)) {
            assertDoesNotThrow(() -> EmailUtils.validateEmail(email));
        } else {
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> EmailUtils.validateEmail(email));
            assertEquals("Invalid email format or domain.", exception.getMessage());
            assertEquals("email", exception.getField());
        }
    }
}
