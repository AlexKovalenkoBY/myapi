package com.wbozon.wb.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.wbozon.wb.api.classes.ProductCard;
import com.wbozon.wb.client.WildberriesCardClient;
import com.wbozon.wb.repository.ProductCardRepository;
import java.util.function.Function;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProductCardService {
    private final WildberriesCardClient client;
    private final ProductCardRepository repository;
    private Map<Long, ProductCard> allCards = new ConcurrentHashMap<>();

    public ProductCardService(WildberriesCardClient client, ProductCardRepository repository) {
        this.client = client;
        this.repository = repository;

        List<ProductCard> existedCards = loadLocalCards();

        Map<Long, ProductCard> loadedMap = existedCards.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        ProductCard::getNmID,
                        Function.identity(),
                        (existing, replacement) -> {
                            System.out.printf("⚠️ Дубликат nmID %d — оставляем существующий%n", existing.getNmID());
                            return existing;
                        }));

        allCards.putAll(loadedMap);

        System.out.printf("✅ Загружено %d карточек при старте%n", loadedMap.size());
        // loadedMap.keySet().forEach(nmID -> System.out.printf("📦 Добавлена карточка nmID: %d%n", nmID));
    }

    public void updateCardsFromWB(Instant updatedAfter) {
        try {

            List<ProductCard> updatedCards = client.fetchUpdatedCards(updatedAfter);
             updatedCards.forEach(c->{allCards.put(c.getNmID(),c);});
            repository.saveStreamed(new ArrayList<>(allCards.values()));

        } catch (Exception e) {
            System.err.println("Ошибка при обновлении карточек: " + e.getMessage());
        }
    }

    public List<ProductCard> loadLocalCardsStreamed() {
        return repository.loadStreamed();
    }

    public List<ProductCard> loadLocalCards() {
        return repository.load();
    }
}
