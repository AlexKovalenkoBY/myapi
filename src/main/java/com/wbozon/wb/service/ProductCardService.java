package com.wbozon.wb.service;

import com.wbozon.wb.api.classes.ProductCard;
import com.wbozon.wb.api.classes.WbSize;
import com.wbozon.wb.api.classes.WareHouseEntity;
import com.wbozon.wb.api.classes.ListGood;
import com.wbozon.wb.api.classes.StockEntity;
import com.wbozon.wb.client.WildberriesCardClient;
import com.wbozon.wb.client.WildberriesPriceClient;
import com.wbozon.wb.client.WildberriesStockClient;
import com.wbozon.wb.repository.ProductCardRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Getter
@Setter
public class ProductCardService {


    private final WildberriesCardClient cardClient;

    private final ProductCardRepository repository;
    private final Map<Long, ProductCard> allCards = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(8); // кастомный пул

    public ProductCardService(WildberriesCardClient cardClient,
                              
      ProductCardRepository repository) {
        this.cardClient = cardClient;
        this.repository = repository;

        List<ProductCard> existedCards = loadLocalCards();
        Map<Long, ProductCard> loadedMap = existedCards.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        ProductCard::getNmID,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        allCards.putAll(loadedMap);
        System.out.printf("✅ Загружено %d карточек при старте%n", loadedMap.size());
    }

    public void updateCardsFromWB(Instant updatedAfter) {
        try {
            List<ProductCard> updatedCards = cardClient.fetchUpdatedCards(updatedAfter);
            updatedCards.forEach(c -> allCards.put(c.getNmID(), c));
            repository.saveStreamed(new ArrayList<>(allCards.values()));
            System.out.printf("🔄 Обновлено %d карточек%n", updatedCards.size());
        } catch (Exception e) {
            System.err.println("❌ Ошибка при обновлении карточек: " + e.getMessage());
        }
    }

    public List<ProductCard> loadLocalCardsStreamed() {
        return repository.loadStreamed();
    }

    public List<ProductCard> loadLocalCards() {
        return repository.load();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
