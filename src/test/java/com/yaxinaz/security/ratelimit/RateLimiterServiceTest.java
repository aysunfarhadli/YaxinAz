package com.yaxinaz.security.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit coverage for the sliding-window limiter itself, independent of whether any controller
 * actually wires it up correctly (that's covered separately by each domain's rate-limit test, e.g.
 * IssueAIControllerTest, AuthControllerTest, IssueControllerTest).
 */
class RateLimiterServiceTest {

    private final RateLimiterService rateLimiterService = new RateLimiterService();

    @Test
    void allowsExactlyMaxPerMinuteThenBlocksTheNext() {
        String key = "test-key-1";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.tryConsume(key, 5), "attempt " + (i + 1) + " should be allowed");
        }
        assertFalse(rateLimiterService.tryConsume(key, 5), "6th attempt should be blocked");
    }

    @Test
    void differentKeysAreTrackedIndependently() {
        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimiterService.tryConsume("key-a", 3));
        }
        assertFalse(rateLimiterService.tryConsume("key-a", 3));

        assertTrue(rateLimiterService.tryConsume("key-b", 3), "a different key must have its own budget");
    }

    @Test
    void singleRequestUnderALimitOfOneIsAllowedAndTheSecondIsNot() {
        String key = "test-key-limit-one";
        assertTrue(rateLimiterService.tryConsume(key, 1));
        assertFalse(rateLimiterService.tryConsume(key, 1));
    }
}
