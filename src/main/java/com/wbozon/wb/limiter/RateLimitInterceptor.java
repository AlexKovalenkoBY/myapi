package com.wbozon.wb.limiter;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimitInterceptor {
    private final long intervalMillis;
    private final AtomicLong lastRequestTime = new AtomicLong(0);

    public RateLimitInterceptor(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public void acquire() {
        long now = Instant.now().toEpochMilli();
        long last = lastRequestTime.get();
        long wait = last + intervalMillis - now;
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ignored) {}
        }
        lastRequestTime.set(Instant.now().toEpochMilli());
    }

    public void handle429(int retrySeconds) {
        try {
            Thread.sleep(retrySeconds * 1000L);
        } catch (InterruptedException ignored) {}
    }
}
