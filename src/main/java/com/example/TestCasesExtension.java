package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

/**
 * JUnit 5 extension that validates Jira-like IDs provided via {@link TestCases}
 * and reports pass/fail status.
 */
public class TestCasesExtension implements TestWatcher, BeforeAllCallback, AfterAllCallback {

    private static final Pattern JIRA_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]+-\\d+$");

    private static class Result {
        final String id;
        final String testName;
        final String status;

        Result(String id, String testName, String status) {
            this.id = id;
            this.testName = testName;
            this.status = status;
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        TestCases annotation = testClass.getAnnotation(TestCases.class);
        if (annotation == null) {
            throw new IllegalStateException("@TestCases is required on class " + testClass.getName());
        }
        String[] ids = annotation.value();
        long testMethodCount = Arrays.stream(testClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Test.class))
                .count();
        if (ids.length != testMethodCount) {
            throw new IllegalArgumentException("Expected " + testMethodCount + " ids but found " + ids.length);
        }
        for (String id : ids) {
            validateId(id);
        }
        ExtensionContext.Store store = context.getStore(Namespace.create(TestCasesExtension.class, testClass));
        store.put("ids", ids);
        store.put("index", new AtomicInteger(0));
        store.put("results", new ArrayList<Result>());
    }

    private String validateId(String id) {
        if (!JIRA_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid test case id: " + id);
        }
        return id;
    }

    private void record(ExtensionContext context, String status) {
        ExtensionContext.Store store = context.getStore(Namespace.create(TestCasesExtension.class, context.getRequiredTestClass()));
        String[] ids = store.get("ids", String[].class);
        AtomicInteger idx = store.get("index", AtomicInteger.class);
        List<Result> results = store.get("results", List.class);
        int i = idx.getAndIncrement();
        if (ids == null || i >= ids.length) {
            throw new IllegalStateException("No ID available for test " + context.getDisplayName());
        }
        String id = ids[i];
        results.add(new Result(id, context.getDisplayName(), status));
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        record(context, "PASSED");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        record(context, "FAILED");
    }

    @Override
    public void afterAll(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(Namespace.create(TestCasesExtension.class, context.getRequiredTestClass()));
        List<Result> results = store.get("results", List.class);
        if (results == null || results.isEmpty()) {
            return;
        }

        boolean enabled = Boolean.parseBoolean(System.getProperty("testcases.report.enabled", "true"));
        if (!enabled) {
            return;
        }

        try {
            Path reportDir = Paths.get("build", "reports");
            Files.createDirectories(reportDir);
            Path output = reportDir.resolve("test-cases.html");
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>TestCase Report</title></head><body>");
            sb.append("<table border=\"1\"><tr><th>ID</th><th>Test</th><th>Status</th></tr>");
            for (Result r : results) {
                sb.append("<tr><td>")
                  .append(r.id)
                  .append("</td><td>")
                  .append(r.testName)
                  .append("</td><td>")
                  .append(r.status)
                  .append("</td></tr>");
            }
            sb.append("</table></body></html>");
            Files.write(output, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write report", e);
        }
    }
}
