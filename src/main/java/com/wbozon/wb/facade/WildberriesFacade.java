package com.wbozon.wb.facade;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;
import com.wbozon.wb.api.classes.ProductCard;
import com.wbozon.wb.client.WildberriesCardClient;
import com.wbozon.wb.repository.ProductCardRepository;
import com.wbozon.wb.service.ProductCardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WildberriesFacade {
    private final ProductCardService service;

    public WildberriesFacade(String token, String filename) {
        WildberriesCardClient client = new WildberriesCardClient(token);
        ProductCardRepository repo = new ProductCardRepository(filename);
        this.service = new ProductCardService(client, repo);
    }

    public Instant getTimeStartForUpdateInstant() {
        List<ProductCard> cards = getLocalCards();
        return cards.stream()
                .map(ProductCard::getUpdatedAt)
                .filter(Objects::nonNull)
                .map(updatedAt -> {
                    try {
                        return Instant.parse(updatedAt);
                    } catch (DateTimeParseException e) {
                        log.warn("Не удалось распарсить дату updatedAt: " + updatedAt, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(Instant.EPOCH); //
    }

    public void update(Instant updatedAfter) {
 long start = System.currentTimeMillis();
    log.info("🚀 Начинаем синхронизацию карточек...");
    service.updateCardsFromWB(updatedAfter);
    long duration = System.currentTimeMillis() - start;
    log.info("🏁 Синхронизация карточек завершена за {} мс", duration);
        // Instant updatedAfter = Instant.now().minus(Duration.ofHours(24));
    }

    public List<ProductCard> getLocalCards() {
        return service.loadLocalCards();
    }

    public void syncAllStocksAndPrices() {
    long start = System.currentTimeMillis();
    log.info("🚀 Начинаем синхронизацию...");


    long duration = System.currentTimeMillis() - start;
    log.info("🏁 Синхронизация завершена за {} мс", duration);
}

}
