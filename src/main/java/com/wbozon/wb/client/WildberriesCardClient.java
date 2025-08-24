package com.wbozon.wb.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wbozon.wb.api.RateLimiter;
import com.wbozon.wb.api.classes.ProductCard;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
@Slf4j
public class WildberriesCardClient {
    private static final int TIMEOUT_SEC = 60;
    private  HttpClient client;
    private final Gson gson = new GsonBuilder().create();
    private final String token;
    private final RateLimiter limiter = new RateLimiter(200);
    private final int MAXREQUESTCOUNT =90000;
    public WildberriesCardClient(String token) {
        this.token = token;
      resetHttpClient();
    }
 private TrustManager[] getTrustAllCerts() {
        return new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
    }

    private void resetHttpClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, getTrustAllCerts(), new SecureRandom());
            this.client = HttpClient.newBuilder()
                //  .proxy(ProxySelector.of(new InetSocketAddress(PROXY_HOST, PROXY_PORT)))
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(TIMEOUT_SEC))
                    .build();
            log.warn("🔄 HttpClient пересоздан из-за превышения лимита карточек.");
        } catch (Exception e) {
            log.error("❌ Ошибка при пересоздании HttpClient", e);
        }
    }

    public List<ProductCard> fetchUpdatedCards(Instant updatedAfter) throws Exception {
        List<ProductCard> result = new ArrayList<>();
        long nmID = 0;
        boolean hasMore = true;
        int tolalRecieved = 0;
        while (hasMore) {
            limiter.acquire();
            String payload = buildPayload(updatedAfter.equals(Instant.EPOCH) ? "" : updatedAfter.toString(), nmID);
            HttpRequest request = buildRequest(payload);
            HttpResponse<String> response = sendWithRetry(request);

            JsonObject root = gson.fromJson(response.body(), JsonObject.class);
            JsonArray cards = root.getAsJsonArray("cards");
            JsonObject cursor = root.getAsJsonObject("cursor");
            tolalRecieved = tolalRecieved + cards.size();
            System.out.printf("\rВсего получено: %d", tolalRecieved);
            if (tolalRecieved % MAXREQUESTCOUNT ==0)     resetHttpClient();
            if (cards == null || cards.size() == 0)
                break;

            for (JsonElement elem : cards) {
                result.add(gson.fromJson(elem, ProductCard.class));
            }

            updatedAfter = Instant.parse(cursor.get("updatedAt").getAsString());
            nmID = cursor.get("nmID").getAsLong();
            hasMore = cards.size() == 100;
        }

        return result;
    }

    private HttpRequest buildRequest(String payload) {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://content-api.wildberries.ru/content/v2/get/cards/list"))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
    }

    private HttpResponse<String> sendWithRetry(HttpRequest request) throws Exception {
        while (true) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            Map<String, List<String>> headers = response.headers().map();

            if (status == 429 || getHeaderInt(headers, "X-Ratelimit-Remaining", 1) == 0) {
                int retry = getHeaderInt(headers, "X-Ratelimit-Retry", 2);
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

    private String buildPayload(String updatedAt, long nmID) {
        JsonObject cursor = new JsonObject();
        cursor.addProperty("limit", 100);
        if (!updatedAt.isEmpty())    cursor.addProperty("updatedAt", updatedAt);
        if (nmID!=0L) cursor.addProperty("nmID", nmID);

        JsonObject settings = new JsonObject();
        settings.add("cursor", cursor);
 JsonObject sort = new JsonObject();
        sort.addProperty("ascending", true);
        settings.add("sort", sort);

        JsonObject filter = new JsonObject();
        filter.addProperty("withPhoto", 1);//только с фото
        settings.add("filter", filter);

        JsonObject root = new JsonObject();
        root.add("settings", settings);
        return gson.toJson(root);
    }
}
