package com.devoxx.agentic.util;


import com.devoxx.agentic.web.WebSearchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RetryUtilTest {

    @Test
    void shouldRetryOnFailure() {
        int[] attempts = {0};
        RetryUtil.withRetry(3, () -> {
            attempts[0]++;
            if (attempts[0] < 2) {
                throw new RuntimeException("Temporary failure");
            }
            return "success";
        }, "Error message");

        assertEquals(2, attempts[0]);
    }

    @Test
    void shouldThrowExceptionAfterMaxRetries() {
        assertThrows(WebSearchException.class, () ->
                RetryUtil.withRetry(3, () -> {
                    throw new RuntimeException("Persistent failure");
                }, "Error message")
        );
    }
}
