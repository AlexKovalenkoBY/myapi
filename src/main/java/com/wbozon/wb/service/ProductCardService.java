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
import java.util.concurrent.atomic.AtomicInteger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProductCardService {
    private final AtomicInteger priceProgress = new AtomicInteger(0);
private final AtomicInteger stockProgress = new AtomicInteger(0);

    private final WildberriesCardClient cardClient;
    private final WildberriesPriceClient priceClient;
    private final WildberriesStockClient stockClient;
    private final ProductCardRepository repository;
    private final Map<Long, ProductCard> allCards = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(8); // кастомный пул

    public ProductCardService(WildberriesCardClient cardClient,
                              WildberriesPriceClient priceClient,
                              WildberriesStockClient stockClient,
                              ProductCardRepository repository) {
        this.cardClient = cardClient;
        this.priceClient = priceClient;
        this.stockClient = stockClient;
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

public void syncPricesAsync(String token) {
    List<Long> nmIDs = new ArrayList<>(allCards.keySet());
    int batchSize = 1000;
    List<List<Long>> batches = partitionList(nmIDs, batchSize);

    priceProgress.set(0);
    int total = batches.size();

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (List<Long> batch : batches) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                Map<Long, ListGood> prices = priceClient.fetchPrices(token, batch);
                for (Map.Entry<Long, ListGood> entry : prices.entrySet()) {
                    ProductCard card = allCards.get(entry.getKey());
                    if (card != null) {
                        card.setPrice(entry.getValue().getPrice());
                        card.setDiscount(entry.getValue().getDiscount());
                    }
                }
                int done = priceProgress.incrementAndGet();
                System.out.printf("📊 Цены: %d/%d пакетов обработано%n", done, total);
            } catch (Exception e) {
                System.err.println("❌ Ошибка в пакете цен: " + e.getMessage());
            }
        }, executor);
        futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    System.out.println("✅ Все цены загружены");
}

public void syncStocksAsync(String token, List<WareHouseEntity> warehouses) {
    List<ProductCard> cards = new ArrayList<>(allCards.values());
    stockProgress.set(0);
    int total = warehouses.size();

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (WareHouseEntity warehouse : warehouses) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                List<StockEntity> stocks = stockClient.fetchStocks(token, warehouse.getId(), cards);
                for (StockEntity stock : stocks) {
                    for (ProductCard card : cards) {
                        for (Size size : card.getSizes()) {
                            if (Arrays.asList(size.getSkus()).contains(stock.getSku())) {
                                size.setStock(stock.getQuantity());
                            }
                        }
                    }
                }
                int done = stockProgress.incrementAndGet();
                System.out.printf("📊 Остатки: %d/%d складов обработано%n", done, total);
            } catch (Exception e) {
                System.err.printf("❌ Ошибка при загрузке остатков для склада %s: %s%n", warehouse.getName(), e.getMessage());
            }
        }, executor);
        futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    System.out.println("✅ Все остатки загружены");
}

    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
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
