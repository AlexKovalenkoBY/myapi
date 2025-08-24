package com.wbozon;

import com.google.gson.*;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;
import java.util.*;
import java.util.concurrent.*;

public class WildberriesApiClientDeepseek {

    private static final String API_URL = "https://content-api.wildberries.ru/content/v2/get/cards/list";
    private static final String API_KEY = "eyJhbGciOiJFUzI1NiIsImtpZCI6IjIwMjUwMTIwdjEiLCJ0eXAiOiJKV1QifQ.eyJlbnQiOjEsImV4cCI6MTc1MzI1ODAwNiwiaWQiOiIwMTk0OGE3OS04MjI5LTc5MzQtYmNjNS1iZGQ2NTcxYTliMmIiLCJpaWQiOjEzMDE0OTk4Niwib2lkIjo3NzgzMDEsInMiOjI2LCJzaWQiOiJiOWI4ZDVmOC1mN2JiLTQ4NDQtYTE4Ni1mY2M3N2ZhMzg0ZGYiLCJ0IjpmYWxzZSwidWlkIjoxMzAxNDk5ODZ9.VonVP5zBxodA-SGkY3iO025r6xaybZqKKnFzQMaddTNeRzDgqUH0y1c8kKSv4YXEI9EZqFbL00BCk_uxJ6wi5w"; // Укажите

    private static final String PROXY_HOST = "8-proxy-1.ds.bapb.internal";
    private static final int PROXY_PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final int REQUEST_DELAY_MS = 600; // 600 ms = 100 requests/minute

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser jsonParser = new JsonParser();

    public static void main(String[] args) {
        List<JsonObject> allCards = new ArrayList<>();
        JsonObject cursor = null;
        int totalRequests = 0;
        
        try (AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient()) {
            ProxyServer proxyServer = new ProxyServer.Builder(PROXY_HOST, PROXY_PORT).build();

            while (true) {
                if (totalRequests >= MAX_REQUESTS_PER_MINUTE) {
                    System.out.println("Достигнут лимит запросов. Ожидание 1 минуты...");
                    TimeUnit.MINUTES.sleep(1);
                    totalRequests = 0;
                }

                String requestBody = buildRequestBody(cursor);
                Response response = executeRequest(asyncHttpClient, proxyServer, requestBody);
                totalRequests++;

                JsonObject responseJson = jsonParser.parse(response.getResponseBody()).getAsJsonObject();
                JsonArray cards = responseJson.getAsJsonArray("cards");
                int total = responseJson.get("total").getAsInt();
                
                // Добавляем карточки в общий список
                cards.forEach(card -> allCards.add(card.getAsJsonObject()));

                // Проверяем условие завершения
                if (cards.size() < 100 || total <= 100) break;

                // Обновляем курсор для следующего запроса
                cursor = responseJson.getAsJsonObject("cursor");
                
                // Добавляем задержку между запросами
                TimeUnit.MILLISECONDS.sleep(REQUEST_DELAY_MS);
            }

            System.out.println("Всего получено карточек: " + allCards.size());
            System.out.println("Первые 100 карточек:");
            allCards.stream().limit(100).forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String buildRequestBody(JsonObject cursor) {
        JsonObject requestBody = new JsonObject();
        JsonObject settings = new JsonObject();
        
        // Настройки сортировки
        JsonObject sort = new JsonObject();
        sort.addProperty("ascending", false);
        if (cursor != null) {
            sort.add("updatedAt", cursor.get("updatedAt"));
            sort.add("nmID", cursor.get("nmID"));
        }
        settings.add("sort", sort);

        // Фильтры
        JsonObject filter = new JsonObject();
        filter.addProperty("withPhoto", -1);
        settings.add("filter", filter);

        // Курсор
        JsonObject cursorObj = new JsonObject();
        cursorObj.addProperty("limit", 100);
        settings.add("cursor", cursorObj);

        requestBody.add("settings", settings);
        return gson.toJson(requestBody);
    }

    private static Response executeRequest(AsyncHttpClient client, ProxyServer proxy, String body) 
        throws ExecutionException, InterruptedException {
        
        Request request = new RequestBuilder("POST")
            .setUrl(API_URL)
            .setHeader("Authorization", API_KEY)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
            .setProxyServer(proxy)
            .build();

        return client.executeRequest(request).get();
    }
}