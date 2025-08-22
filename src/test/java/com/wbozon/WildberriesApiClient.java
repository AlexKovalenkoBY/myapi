package com.wbozon;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WildberriesApiClient {

    private static final String API_URL =
            "https://content-api.wildberries.ru/content/v2/get/cards/list";
    private static final String API_KEY =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6IjIwMjUwMTIwdjEiLCJ0eXAiOiJKV1QifQ.eyJlbnQiOjEsImV4cCI6MTc1MzI1ODAwNiwiaWQiOiIwMTk0OGE3OS04MjI5LTc5MzQtYmNjNS1iZGQ2NTcxYTliMmIiLCJpaWQiOjEzMDE0OTk4Niwib2lkIjo3NzgzMDEsInMiOjI2LCJzaWQiOiJiOWI4ZDVmOC1mN2JiLTQ4NDQtYTE4Ni1mY2M3N2ZhMzg0ZGYiLCJ0IjpmYWxzZSwidWlkIjoxMzAxNDk5ODZ9.VonVP5zBxodA-SGkY3iO025r6xaybZqKKnFzQMaddTNeRzDgqUH0y1c8kKSv4YXEI9EZqFbL00BCk_uxJ6wi5w"; // Укажите
                                                                                                                                                                                                                                                                                                                                                                                                                  // ваш
                                                                                                                                                                                                                                                                                                                                                                                                                  // API-ключ
                                                                                                                                                                                                                                                                                                                                                                                                                  // Wildberries
    private static final int LIMIT = 100; // Лимит карточек на запрос

    // Настройки прокси (замените на ваши данные)
    private static final String PROXY_HOST = "8-proxy-1.ds.bapb.internal"; // Адрес прокси
    private static final int PROXY_PORT = 8080; // Порт прокси

    public static void main(String[] args) {
        try {
            List<Map<String, Object>> allCards = fetchAllCards();
            System.out.println("Общее количество карточек: " + allCards.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, Object>> fetchAllCards() throws Exception {
        Gson gson = new Gson();
        List<Map<String, Object>> allCards = new ArrayList<>();
        String lastCursor = ""; // Начальное значение курсора
        disableSSLVerification();
        // Устанавливаем прокси
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT));

        while (true) {
            // Создание тела запроса
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("limit", LIMIT);
            if (!lastCursor.isEmpty()) {
                requestBody.addProperty("cursor", lastCursor);
            }

            // Выполнение POST-запроса с прокси
            HttpURLConnection connection =
                    (HttpURLConnection) new URL(API_URL).openConnection(proxy);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(os)) {
                writer.write(requestBody.toString());
                writer.flush();
            }

            // Чтение ответа
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Ошибка API: HTTP код " + responseCode);
            }

            try (BufferedReader br =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                JsonObject responseJson = gson.fromJson(br, JsonObject.class);

                // Извлечение данных карточек
                JsonArray cards = responseJson.getAsJsonArray("cards");
                Type cardType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                List<Map<String, Object>> cardList = gson.fromJson(cards, cardType);
                allCards.addAll(cardList);

                // Проверяем, есть ли следующая страница
                if (!responseJson.has("nextCursor")
                        || responseJson.get("nextCursor").isJsonNull()) {
                    break; // Если nextCursor отсутствует, выходим из цикла
                }
                lastCursor = responseJson.get("nextCursor").getAsString();
            } finally {
                connection.disconnect();
            }
        }

        return allCards;
    }

    public static void disableSSLVerification() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }};

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}


