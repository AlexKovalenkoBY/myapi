package com.wbozon.wb.client;

import com.google.common.util.concurrent.RateLimiter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterManager {
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public RateLimiter getLimiter(String key, double permitsPerSecond) {
        return limiters.computeIfAbsent(key, k -> RateLimiter.create(permitsPerSecond));
    }

    public void adjustAfter429(String key) {
        RateLimiter limiter = limiters.get(key);
        if (limiter != null) {
            double currentRate = limiter.getRate();
            limiter.setRate(Math.max(1.0, currentRate * 0.5));
        }
    }
}
