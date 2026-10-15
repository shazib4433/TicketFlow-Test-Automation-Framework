package utils;

import api.TicketApiClient;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestExecutionListener implements ITestListener {

    private final List<String> failures = new ArrayList<>();
    private int passed;
    private int skipped;

    @Override
    public void onTestSuccess(ITestResult result) {
        passed++;
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        skipped++;
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String apiContext = TicketApiClient.lastRequestContext();
        String failure = "%s.%s: %s%s".formatted(
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                result.getThrowable() == null ? "unknown failure" : result.getThrowable().getMessage(),
                apiContext.isBlank() ? "" : " | " + apiContext);
        failures.add(failure);
        System.err.println("TEST FAILURE: " + failure);
    }

    @Override
    public void onFinish(ITestContext context) {
        Path report = Path.of("target", "test-reports", "execution-summary.log");
        List<String> lines = new ArrayList<>();
        lines.add("TicketFlow Test Execution Summary");
        lines.add("Finished: " + Instant.now());
        lines.add("Passed: " + passed);
        lines.add("Failed: " + failures.size());
        lines.add("Skipped: " + skipped);
        if (!failures.isEmpty()) {
            lines.add("Failures:");
            lines.addAll(failures);
        }
        try {
            Files.createDirectories(report.getParent());
            Files.write(report, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            System.err.println("Unable to write test execution summary: " + exception.getMessage());
        }
    }
}
