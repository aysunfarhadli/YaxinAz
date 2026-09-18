package com.yaxinaz.security.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory, per-key sliding-window (1 minute) rate limiter. Deliberately not backed by
 * Redis (spec section 23 explicitly forbids adding Redis just for this) - fine for a single-instance
 * course deployment.
 */
@Service
public class RateLimiterService {

    private final Cache<String, AtomicInteger> counters = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .build();

    public boolean tryConsume(String key, int maxPerMinute) {
        AtomicInteger counter = counters.get(key, k -> new AtomicInteger(0));
        int current = counter.incrementAndGet();
        return current <= maxPerMinute;
    }
}
