package com.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@TestCases({"AUTH-1023", "AUTH-9999"})
public class SampleTest {

    @Test
    void shouldPassValidation() {
        Assertions.assertTrue(true);
    }

    @Test
    void shouldAlsoPass() {
        Assertions.assertEquals(4, 2 + 2);
    }
}
