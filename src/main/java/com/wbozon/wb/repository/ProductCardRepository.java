package com.wbozon.wb.repository;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.wbozon.wb.api.classes.ProductCard;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
@Slf4j
public class ProductCardRepository {
    private final Path filePath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ProductCardRepository(String filename) {
        this.filePath = Paths.get(filename);
    }
 public void save(List<ProductCard> cards) {
       long start = System.currentTimeMillis();
        log.info("🚀 Начинаем ОБЫЧНУЮ запись карточек в файл ...");
       try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
           gson.toJson(cards, writer);
             long duration = System.currentTimeMillis() - start;
            log.info("🏁 Обычная Запись карточек в файла завершено за {} мс", duration);
       } catch (IOException e) {
           System.err.println("Ошибка при сохранении карточек: " + e.getMessage());
       }
   }

   public List<ProductCard> load() {
        long start = System.currentTimeMillis();
        log.info("🚀 Начинаем чтение карточек из файла ...");
       
        if (!Files.exists(filePath)) return new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            Type type = new TypeToken<List<ProductCard>>() {}.getType();
            long duration = System.currentTimeMillis() - start;
            log.info("🏁 Чтение карточек из файла завершено за {} мс", duration);
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке карточек: " + e.getMessage());
            return new ArrayList<>();
        }
   }
    // Потоковое сохранение
    public void saveStreamed(List<ProductCard> cards) {
          long start = System.currentTimeMillis();
        log.info("🚀 Начинаем ПОТОКОВОЕ сохранение карточек в файл ...");
        try (BufferedWriter writer = Files.newBufferedWriter(filePath);
             JsonWriter jsonWriter = new JsonWriter(writer)) {

            jsonWriter.beginArray();
            for (ProductCard card : cards) {
                gson.toJson(card, ProductCard.class, jsonWriter);
            }
            jsonWriter.endArray();

            System.out.println("✅  Сохранено " + cards.size() + " карточек в файл: " + filePath);
             long duration = System.currentTimeMillis() - start;
        log.info("🏁 Потоковое сохранение карточек завершено за {} мс", duration);
        } catch (IOException e) {
            System.err.println("❌ Ошибка при сохранении карточек: " + e.getMessage());
        }
    }

    // Потоковая загрузка
    public List<ProductCard> loadStreamed() {
        List<ProductCard> result = new ArrayList<>();
        if (!Files.exists(filePath)) {
            System.out.println("⚠ Файл не найден: " + filePath);
            return result;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath);
             JsonReader jsonReader = new JsonReader(reader)) {

            jsonReader.beginArray();
            int count = 0;

            while (jsonReader.hasNext()) {
                ProductCard card = gson.fromJson(jsonReader, ProductCard.class);
                result.add(card);
                count++;

                if (count % 1000 == 0) {
                    System.out.printf("📦 Загружено: %d карточек...\r", count);
                }
            }

            jsonReader.endArray();
            System.out.println("\n✅ Загружено всего: " + count + " карточек из файла: " + filePath);

        } catch (IOException e) {
            System.err.println("❌ Ошибка при загрузке карточек: " + e.getMessage());
        }

        return result;
    }
}
