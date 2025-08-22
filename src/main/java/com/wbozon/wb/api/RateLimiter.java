package com.wbozon.wb.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimiter {
    private String apiName;
    private int maxRequests;
    private final long intervalMs;
    private final int burstSize;
    private final long minIntervalMs;
    private final Object lock = new Object();
    private int remaining;
    private long lastRequestTime;
    private long nextResetTime;

    public RateLimiter(String apiName, int maxRequests, long intervalMs, int burstSize, long minIntervalMs) {
        this.apiName = apiName;
        this.maxRequests = maxRequests;
        this.intervalMs = intervalMs;
        this.burstSize = burstSize;
        this.minIntervalMs = minIntervalMs;
        this.remaining = burstSize;
        this.lastRequestTime = 0;
        this.nextResetTime = System.currentTimeMillis() + intervalMs;
    }

    public void acquire() throws InterruptedException {
        synchronized (lock) {
            long now = System.currentTimeMillis();
            
            // Сброс счетчика если прошел интервал
            if (now >= nextResetTime) {
                remaining = burstSize;
                nextResetTime = now + intervalMs;
            }
            
            // Расчет времени до следующего доступного запроса
            long timeSinceLast = now - lastRequestTime;
            
            if (remaining <= 0) {
                // Если лимит исчерпан, ждем сброса
                long waitTime = nextResetTime - now;
                if (waitTime > 0) {
                    Thread.sleep(waitTime);
                }
                remaining = burstSize;
                nextResetTime = System.currentTimeMillis() + intervalMs;
            } else if (timeSinceLast < minIntervalMs) {
                // Соблюдаем минимальный интервал между запросами
                Thread.sleep(minIntervalMs - timeSinceLast);
            }
            
            remaining--;
            lastRequestTime = System.currentTimeMillis();
        }
    }

    public void adjustForConflict() {
        synchronized (lock) {
            remaining = Math.max(0, remaining - 4); // 409 учитывается как 5 запросов
        }
    }

    // Добавляем недостающий метод
    public long calculateDynamicDelay() {
        synchronized (lock) {
            // Простая логика расчета задержки на основе оставшегося лимита
            if (remaining <= maxRequests * 0.2) {
                // Меньше 20% лимита - увеличиваем задержку
                return minIntervalMs * 2;
            }
            return minIntervalMs;
        }
    }
}