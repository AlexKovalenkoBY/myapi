package com.wbozon.wb.api;

public class RateLimiter {
    private final long intervalMillis;
    private long lastRequestTime = 0;

    public RateLimiter(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public synchronized void acquire() {
        long now = System.currentTimeMillis();
        long waitTime = lastRequestTime + intervalMillis - now;
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ignored) {}
        }
        lastRequestTime = System.currentTimeMillis();
    }
}
