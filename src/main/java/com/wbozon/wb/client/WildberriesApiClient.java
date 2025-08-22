package com.wbozon.wb.client;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.wbozon.wb.api.RateLimiter;
import com.wbozon.wb.api.RetryHandler;

public class WildberriesApiClient {
    private final RateLimiter rateLimiter = new RateLimiter(200); // 200ms между запросами
    private final RetryHandler retryHandler = new RetryHandler(3, 1000); // 3 попытки, 1 секунда

    public String fetchWarehouses() {
        rateLimiter.acquire();
        return retryHandler.executeWithRetry(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.wildberries.ru/api/v1/warehouses"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                throw new HttpException(429, "Rate limit exceeded");
            }
            return response.body();
        });
    }
}
