package com.devoxx.agentic.util;

import com.devoxx.agentic.web.WebSearchException;

import java.util.concurrent.Callable;

public class RetryUtil {
    public static <T> T withRetry(int maxRetries, Callable<T> operation, String errorMessage) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                return operation.call();
            } catch (Exception e) {
                attempts++;
                if (attempts == maxRetries) {
                    throw new WebSearchException(errorMessage, e);
                }
                try {
                    Thread.sleep(2000L * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new WebSearchException("Retry interrupted", ie);
                }
            }
        }
        throw new WebSearchException(errorMessage, null);
    }
}
