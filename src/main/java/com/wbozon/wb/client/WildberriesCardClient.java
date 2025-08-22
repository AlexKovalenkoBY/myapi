package com.wbozon.wb.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wbozon.wb.api.RateLimiter;
import com.wbozon.wb.api.classes.ProductCard;

public class WildberriesCardClient {
   private final HttpClient client;
   private final Gson gson = new GsonBuilder().create();
   private final String token;
   private final RateLimiter limiter = new RateLimiter(200);

   public WildberriesCardClient(String token) {
       this.token = token;
       this.client = HttpClient.newBuilder()
           .connectTimeout(Duration.ofSeconds(60))
           .build();
   }

   public List<ProductCard> fetchUpdatedCards(Instant updatedAfter) throws Exception {
       List<ProductCard> result = new ArrayList<>();
       long nmID = 0;
       boolean hasMore = true;

       while (hasMore) {
           limiter.acquire();
           String payload = buildPayload(updatedAfter.toString(), nmID);
           HttpRequest request = buildRequest(payload);
           HttpResponse<String> response = sendWithRetry(request);

           JsonObject root = gson.fromJson(response.body(), JsonObject.class);
           JsonArray cards = root.getAsJsonArray("cards");
           JsonObject cursor = root.getAsJsonObject("cursor");

           if (cards == null || cards.size() == 0) break;

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
       cursor.addProperty("updatedAt", updatedAt);
       cursor.addProperty("nmID", nmID);

       JsonObject settings = new JsonObject();
       settings.add("cursor", cursor);

       JsonObject root = new JsonObject();
       root.add("settings", settings);
       return gson.toJson(root);
   }
}
