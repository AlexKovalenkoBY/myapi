package com.wbozon.wb.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wbozon.wb.api.classes.ProductCard;

public class ProductCardRepository {
   private final Path filePath;
   private final Gson gson = new GsonBuilder().create();

   public ProductCardRepository(String filename) {
       this.filePath = Paths.get(filename);
   }

   public void save(List<ProductCard> cards) {
       try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
           gson.toJson(cards, writer);
       } catch (IOException e) {
           System.err.println("Ошибка при сохранении карточек: " + e.getMessage());
       }
   }

   public List<ProductCard> load() {
       if (!Files.exists(filePath)) return new ArrayList<>();
       try (BufferedReader reader = Files.newBufferedReader(filePath)) {
        Type type = new TypeToken<List<ProductCard>>() {}.getType();
           return gson.fromJson(reader, type);
       } catch (IOException e) {
           System.err.println("Ошибка при загрузке карточек: " + e.getMessage());
           return new ArrayList<>();
       }
   }
}
