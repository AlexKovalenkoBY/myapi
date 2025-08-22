package com.wbozon.wb.client;

import com.wbozon.wb.api.RateLimiter;

public class RateLimiterManager {
   private final RateLimiter limiter;

   public RateLimiterManager(int permitsPerSecond) {
       this.limiter = RateLimiter.create(permitsPerSecond);
   }

   public void acquire() throws InterruptedException {
       limiter.acquire();
   }

   public void adjustAfter429() {
       // Реакция на ошибку 429 — например, временное снижение скорости
       limiter.setRate(Math.max(1, limiter.getRate() / 2));
   }
}
