package com.wbozon.wb.client;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.wbozon.wb.api.RateLimiter;
// import com.wbozon.models.*;
// import com.wbozon.wb.api.classes.*;
import com.wbozon.wb.api.classes.ApiResponse;
import com.wbozon.wb.api.classes.Cursor;
import com.wbozon.wb.api.classes.ListGood;
import com.wbozon.wb.api.classes.PriceApiResponse;
import com.wbozon.wb.api.classes.ProductCard;
import com.wbozon.wb.api.classes.StockEntity;
import com.wbozon.wb.api.classes.StocksResponse;
import com.wbozon.wb.api.classes.WareHouseEntity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WildberriesApiClient {
    private final HttpClient client;
    private final Gson gson;
    private final String authToken;
    private final RateLimiterManager rateLimiterManager;

    private static final int TIMEOUT_SEC = 60;
    private static final String CARDS_API_URL = "https://content-api.wildberries.ru/content/v2/get/cards/list";
    private static final String PRICES_API_URL = "https://discounts-prices-api.wildberries.ru/api/v2/list/goods/filter";
    private static final String WAREHOUSES_API_URL = "https://marketplace-api.wildberries.ru/api/v3/warehouses";
    private static final String STOCKS_API_URL = "https://marketplace-api.wildberries.ru/api/v3/stocks/";

    public WildberriesApiClient(String authToken, RateLimiterManager rateLimiterManager) {
        this.authToken = authToken;
        this.rateLimiterManager = rateLimiterManager;
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEC))
                .build();
    }

    public List<ProductCard> fetchCards(Instant updatedAfter) throws IOException, InterruptedException {
        Cursor cursor = new Cursor();
        cursor.setUpdatedAt(updatedAfter.equals(Instant.MIN) ? null : updatedAfter.toString());
        cursor.setLimit(100);

        List<ProductCard> result = new ArrayList<>();
        boolean hasMore = true;

        while (hasMore) {
            String body = gson.toJson(Map.of("cursor", cursor));
            HttpRequest request = buildPostRequest(CARDS_API_URL, body);
            ApiResponse response = executeRequest(request, ApiResponse.class, "CARDS");

            if (response.getCards() != null) {
                result.addAll(response.getCards());
                cursor = response.getCursor();
                hasMore = response.getCards().size() >= 100 && cursor != null;
            } else {
                hasMore = false;
            }
        }

        return result;
    }

    public ListGood[] fetchPrices(int offset, int limit) throws IOException, InterruptedException {
        String url = String.format("%s?limit=%d&offset=%d", PRICES_API_URL, limit, offset);
        HttpRequest request = buildGetRequest(url);
        PriceApiResponse response = executeRequest(request, PriceApiResponse.class, "PRICES");
        return Optional.ofNullable(response.getData()).map(PriceApiResponse::getData::getListGoods).orElse(new ListGood[0]);
    }

    public List<WareHouseEntity> fetchWarehouses() throws IOException, InterruptedException {
        HttpRequest request = buildGetRequest(WAREHOUSES_API_URL);
        Type type = new TypeToken<List<WareHouseEntity>>() {}.getType();
        return executeRequest(request, type, "WAREHOUSES");
    }

    public List<StockEntity> fetchStocks(List<String> skus, String warehouseId) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        JsonArray array = new JsonArray();
        skus.forEach(array::add);
        body.add("skus", array);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STOCKS_API_URL + warehouseId))
                .header("Authorization", authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                .build();

        StocksResponse response = executeRequest(request, StocksResponse.class, "STOCKS");
        return response.getStocks() != null ? Arrays.asList(response.getStocks()) : Collections.emptyList();
    }

    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", authToken)
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                .build();
    }

    private HttpRequest buildPostRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                .build();
    }

    private <T> T executeRequest(HttpRequest request, Type type, String apiName) throws IOException, InterruptedException {
        RateLimiter limiter = rateLimiterManager.getLimiter(apiName);
        limiter.acquire();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateRateLimitHeaders(response, limiter);

        if (response.statusCode() == 429) {
            limiter.adjustAfter429();
            throw new IOException("Rate limit exceeded for " + apiName);
        }

        if (response.statusCode() != 200) {
            throw new IOException("HTTP error " + response.statusCode() + ": " + response.body());
        }

        return gson.fromJson(response.body(), type);
    }

    private void updateRateLimitHeaders(HttpResponse<String> response, RateLimiter limiter) {
        response.headers().firstValue("X-RateLimit-Remaining").ifPresent(val -> {
            try {
                limiter.setRemaining(Integer.parseInt(val));
            } catch (NumberFormatException ignored) {}
        });
        response.headers().firstValue("X-RateLimit-Limit").ifPresent(val -> {
            try {
                limiter.setMaxRequests(Integer.parseInt(val));
            } catch (NumberFormatException ignored) {}
        });
    }
}
