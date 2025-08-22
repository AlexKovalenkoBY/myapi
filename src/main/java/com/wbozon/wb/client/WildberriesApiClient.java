package com.wbozon.wb.client;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.hc.core5.http.HttpException;

import java.io.IOException;
import java.net.URI;
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

            HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (response.statusCode() == 429) {
                throw new HttpException("Rate limit exceeded");
            }
            return response.body();
        });
    }
}
