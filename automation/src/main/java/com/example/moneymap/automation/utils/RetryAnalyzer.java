package com.example.moneymap.automation.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * TestNG retry analyser that automatically re-runs failed tests up to MAX_RETRIES times.
 * This helps eliminate flaky failures caused by transient emulator/network issues.
 *
 * Usage: annotate @Test with retryAnalyzer = RetryAnalyzer.class
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    /** Maximum number of retry attempts per failing test */
    private static final int MAX_RETRIES = 2;

    /** Per-instance retry counter (TestNG creates a new instance per test method) */
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            LogUtil.log(String.format(
                "RETRY [%d/%d] — %s — Reason: %s",
                retryCount, MAX_RETRIES,
                result.getName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "unknown"
            ));
            return true; // Signal TestNG to re-run this test
        }
        return false; // Exhausted retries — mark as permanently failed
    }
}
