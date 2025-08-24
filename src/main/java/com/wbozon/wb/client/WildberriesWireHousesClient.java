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
import java.util.logging.Logger;

public class WildberriesWireHousesClient {
    private static final Logger logger = Logger.getLogger(WildberriesWireHousesClient.class.getName());
    private static final String WAREHOUSES_URL = 
        "https://marketplace-api.wildberries.ru/api/v3/warehouses";
    private static final int TIMEOUT_SEC = 30;

    private final HttpClient httpClient;
    private final RateLimiter rateLimiter = new RateLimiter(200);
    private final RetryHandler retryHandler = new RetryHandler(3, 1000);
    private final Gson gson = new GsonBuilder().create();
    private final String token;

    public WildberriesWireHousesClient(String token) {
        this.token = token;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEC))
                .build();
    }

public List<WareHouseEntity> fetchWarehouses() {
    rateLimiter.acquire();
    
    return retryHandler.executeWithRetry(() -> {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WAREHOUSES_URL))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                    .GET()
                    .build();

            logger.info("Sending request to: " + WAREHOUSES_URL);
            
            // Исправлено: используем BodyHandlers.ofString() вместо класса
            HttpResponse<String> resp = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()  // Правильный обработчик
            );

            int status = resp.statusCode();
            String responseBody = resp.body();  // Теперь это String
                
            logger.info("Response status: " + status);
            logger.info("Response body: " + responseBody);

            if (status == 200) {
                 JsonArray dataArray = gson.fromJson(responseBody, JsonArray.class);
                   List<WareHouseEntity> list = new ArrayList<>(dataArray.size());
                  for (JsonElement elem : dataArray) {
                    try {
                        WareHouseEntity warehouse = gson.fromJson(elem, WareHouseEntity.class);
                        list.add(warehouse);
                    } catch (Exception e) {
                        logger.warning("Failed to parse warehouse element: " + elem.toString());
                    }
                }
                return list;
               

            } else if (status == 401) {
                throw new IOException("Unauthorized (401) - Invalid token");
            } else if (status == 403) {
                throw new IOException("Forbidden (403) - No access to warehouses");
            } else if (status == 429) {
                throw new IOException("Rate limit exceeded (429)");
            } else {
                throw new IOException("Unexpected HTTP status: " + status + ", Response: " + responseBody);
            }

        } catch (IOException | InterruptedException e) {
            logger.severe("Error in fetchWarehouses: " + e.getMessage());
            throw new RuntimeException("Error fetching warehouses: " + e.getMessage(), e);
        }
    });
}
    // Метод для тестирования соединения
    public boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WAREHOUSES_URL))
                    .header("Authorization", "Bearer " + token)
                    .HEAD()
                    .build();
            
            HttpResponse<Void> resp = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return resp.statusCode() != 404 && resp.statusCode() != 403;
        } catch (Exception e) {
            return false;
        }
    }
}