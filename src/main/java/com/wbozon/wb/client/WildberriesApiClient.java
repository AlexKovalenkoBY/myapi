package com.wbozon.wb.client;

import com.wbozon.wb.limiter.RateLimitInterceptor;
import java.net.http.*;
import java.net.URI;
import java.util.Map;

public class WildberriesApiClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String token;
    private final RateLimitInterceptor limiter = new RateLimitInterceptor(200);

    public WildberriesApiClient(String token) {
        this.token = token;
    }

    public String fetchWarehouses() throws Exception {
        limiter.acquire();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://supplies-api.wildberries.ru/api/v2/warehouses"))
            .header("Authorization", token)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            int retry = parseRetryHeader(response.headers().map());
            limiter.handle429(retry);
            return fetchWarehouses(); // retry
        }

        return response.body();
    }

    private int parseRetryHeader(Map<String, java.util.List<String>> headers) {
        return headers.getOrDefault("X-Ratelimit-Retry", java.util.List.of("2"))
                      .stream().findFirst().map(Integer::parseInt).orElse(2);
    }
}
