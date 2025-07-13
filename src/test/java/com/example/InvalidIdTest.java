package com.example;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

@Disabled("Demonstrates invalid id failure")
@TestCases({"invalid"})
public class InvalidIdTest {

    @Test
    void invalidIdShouldThrow() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Assertions.assertTrue(true);
        });
        Assertions.assertEquals("Invalid test case id: invalid", ex.getMessage());
    }
}
