package com.wbozon.wb.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.wbozon.wb.api.RateLimiter;
import com.wbozon.wb.api.RetryHandler;

public class WildberriesApiClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final RateLimiter rateLimiter = new RateLimiter(200); // 200ms между запросами
    private final RetryHandler retryHandler = new RetryHandler(3, 1000); // 3 попытки, 1 секунда
    private final String token;

    public WildberriesApiClient(String token) {
        this.token = token;
    }

    public String fetchWarehouses() {
        rateLimiter.acquire();

        return retryHandler.executeWithRetry(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.wildberries.ru/api/v1/warehouses"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                int statusCode = response.statusCode();
                if (statusCode == 200) {
                    return response.body();
                } else if (statusCode == 429) {
                    throw new IOException("Rate limit exceeded (429)");
                } else {
                    throw new IOException("Unexpected response code: " + statusCode);
                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Ошибка при выполнении запроса: " + e.getMessage(), e);
            }
        });
    }
}
