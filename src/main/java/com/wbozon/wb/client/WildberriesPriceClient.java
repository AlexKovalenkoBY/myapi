package com.wbozon.wb.client;

import com.google.gson.*;
import com.wbozon.wb.api.RateLimiter;
import com.wbozon.wb.api.classes.ListGood;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.net.URI;
import java.net.http.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;

@Slf4j
public class WildberriesPriceClient {
    private static final String URL = "https://suppliers-api.wildberries.ru/api/v2/list/goods/filter";
    private static final int TIMEOUT_SEC = 60;
    private final HttpClient client;
    private final Gson gson = new GsonBuilder().create();
    private final String token;
    private final RateLimiter limiter = new RateLimiter(1000); // 1000 запросов/мин

    public WildberriesPriceClient(String token) {
        this.token = token;
        this.client = createHttpClient();
    }

    private HttpClient createHttpClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, getTrustAllCerts(), new SecureRandom());
            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(TIMEOUT_SEC))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании HttpClient", e);
        }
    }

    private TrustManager[] getTrustAllCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
    }

    public Map<Long, ListGood> fetchPrices(List<Long> nmIDs) throws Exception {
        limiter.acquire();

        JsonObject filter = new JsonObject();
        JsonArray ids = new JsonArray();
        nmIDs.forEach(ids::add);
        filter.add("nmIDs", ids);

        JsonObject root = new JsonObject();
        root.add("filter", filter);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(root)))
                .build();

        HttpResponse<String> response = sendWithRetry(request);
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        JsonArray goods = json.getAsJsonObject("data").getAsJsonArray("listGoods");

        Map<Long, ListGood> result = new HashMap<>();
        for (JsonElement elem : goods) {
            ListGood good = gson.fromJson(elem, ListGood.class);
            result.put(good.getNmID(), good);
        }

        return result;
    }

    private HttpResponse<String> sendWithRetry(HttpRequest request) throws Exception {
        while (true) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            Map<String, List<String>> headers = response.headers().map();

            if (status == 429 || getHeaderInt(headers, "X-Ratelimit-Remaining", 1) == 0) {
                int retry = getHeaderInt(headers, "X-Ratelimit-Retry", 2);
                log.warn("⚠️ 429 Too Many Requests. Повтор через {} сек", retry);
                Thread.sleep(retry * 1000L);
                continue;
            }

            return response;
        }
    }

    private int getHeaderInt(Map<String, List<String>> headers, String key, int defaultValue) {
        try {
            return Integer.parseInt(headers.getOrDefault(key, List.of(String.valueOf(defaultValue))).get(0));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
