package com.wbozon.wb.facade;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;
import com.wbozon.wb.api.classes.ProductCard;
import com.wbozon.wb.client.WildberriesCardClient;
import com.wbozon.wb.client.WildberriesPriceClient;
import com.wbozon.wb.client.WildberriesStockClient;
import com.wbozon.wb.client.WildberriesWireHousesClient;
import com.wbozon.wb.repository.ProductCardRepository;
import com.wbozon.wb.service.ProductCardService;
import com.wbozon.wb.service.WildberriesProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WildberriesFacade {
    private final ProductCardService scardService;
    private final WildberriesProductService wildberriesProductService;

    public WildberriesFacade(String token, String filename) {
        ProductCardRepository repo = new ProductCardRepository(filename);
        WildberriesCardClient client = new WildberriesCardClient(token);
        WildberriesStockClient stockClient = new WildberriesStockClient(token);
        WildberriesWireHousesClient wildberriesWireHousesClient = new WildberriesWireHousesClient(token);
        WildberriesPriceClient priceClient = new WildberriesPriceClient(token);
        this.scardService = new ProductCardService(client, repo);
        this.wildberriesProductService = new WildberriesProductService(scardService, wildberriesWireHousesClient,
                priceClient, stockClient);
        int tt = 0;
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
        log.info("🚀 Начинаем обновление карточек из product_cards.json  ...");
        scardService.updateCardsFromWB(updatedAfter);
        long duration = System.currentTimeMillis() - start;
        log.info("🏁 Обновление карточек завершено за {} мс", duration);
        // Instant updatedAfter = Instant.now().minus(Duration.ofHours(24));
    }

    public List<ProductCard> getLocalCards() {
        return scardService.loadLocalCards();
    }

    public List<ProductCard> getLocalCardsStreamed() {
        return scardService.loadLocalCardsStreamed();
    }

    public void syncAllStocksAndPrices() {
        long start = System.currentTimeMillis();
        log.info("🚀 Начинаем синхронизацию...");
        wildberriesProductService.syncStocksAsync();
        long duration = System.currentTimeMillis() - start;
        log.info("🏁 Синхронизация завершена за {} мс", duration);
    }

}
