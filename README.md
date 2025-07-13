# testcasereporter

Simple annotation based test case reporter using JUnit 5. Apply `@TestCases` on
a test class to automatically register the reporter and provide Jira IDs for each
test method. After execution an HTML report is written to `build/reports/test-cases.html`.
The report generation can be disabled by running tests with the system property
`-Dtestcases.report.enabled=false`.

Usage example:

```java
@TestCases({"AUTH-1023", "AUTH-9999"})
class SampleTest {

    @Test
    void shouldPassValidation() {
        Assertions.assertTrue(true);
    }

    @Test
    void shouldAlsoPass() {
        Assertions.assertEquals(4, 2 + 2);
    }
}
```

Running the tests generates the HTML report with the Jira id and the status of
each test.
