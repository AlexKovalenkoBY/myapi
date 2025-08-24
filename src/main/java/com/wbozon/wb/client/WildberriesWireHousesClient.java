package com.wbozon.wb.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wbozon.wb.api.RateLimiter;
import com.wbozon.wb.api.RetryHandler;
import com.wbozon.wb.api.classes.WareHouseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WildberriesWireHousesClient {
    private static final String WAREHOUSES_URL = 
        "https://suppliers-api.wildberries.ru/api/v3/warehouses?locale=ru";
    private static final int TIMEOUT_SEC = 30;

    private final HttpClient httpClient;
    private final RateLimiter rateLimiter = new RateLimiter(200);      // 200 ms between requests
    private final RetryHandler retryHandler = new RetryHandler(3, 1000); // 3 attempts, 1s base delay
    private final Gson gson = new GsonBuilder().create();
    private final String token;

    public WildberriesWireHousesClient(String token) {
        this.token = token;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEC))
                .build();
    }

    /**
     * Fetches the list of seller warehouses via GET /api/v3/warehouses
     * Applies rate limiting and retries on 429 or transient IO errors.
     */
    public List<WareHouseEntity> fetchWarehouses() {
        // 1) Throttle by milliseconds
        rateLimiter.acquire();

        // 2) Execute with retry logic
        return retryHandler.executeWithRetry(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WAREHOUSES_URL))
                    .header("Authorization", token)
                    .GET()
                    .build();

            try {
                HttpResponse<String> resp = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                int status = resp.statusCode();
                if (status == 200) {
                    // parse JSON: { "data": [ {...}, {...} ], "error": false, ... }
                    JsonObject root = gson.fromJson(resp.body(), JsonObject.class);
                    JsonArray data = root.getAsJsonArray("data");

                    List<WareHouseEntity> list = new ArrayList<>(data.size());
                    for (JsonElement elem : data) {
                        list.add(gson.fromJson(elem, WareHouseEntity.class));
                    }
                    return list;

                } else if (status == 429) {
                    // Too Many Requests -> propagate for retry
                    throw new IOException("Rate limit exceeded (429)");
                } else {
                    throw new IOException("Unexpected HTTP status: " + status);
                }

            } catch (IOException | InterruptedException e) {
                // wrap into unchecked to let RetryHandler catch/retry
                throw new RuntimeException("Error fetching warehouses: " + e.getMessage(), e);
            }
        });
    }
}
