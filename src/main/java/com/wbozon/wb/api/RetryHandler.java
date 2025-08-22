package com.wbozon.wb.api;

import java.util.function.Supplier;

public class RetryHandler {
    private final int maxRetries;
    private final long retryDelayMillis;

    public RetryHandler(int maxRetries, long retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
    }

    public <T> T executeWithRetry(Supplier<T> action) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                lastException = e;

                // Пример: если это ошибка 429, можно парсить сообщение
                if (e.getMessage() != null && e.getMessage().contains("429") && attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMillis);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ignored);
                    }
                } else {
                    throw e;
                }
            }
        }

        throw new RuntimeException("Max retries exceeded", lastException);
    }
}
