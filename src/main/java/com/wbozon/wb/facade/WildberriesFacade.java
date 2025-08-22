package com.wbozon.wb.facade;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.wbozon.wb.api.classes.ProductCard;
import com.wbozon.wb.client.WildberriesCardClient;
import com.wbozon.wb.repository.ProductCardRepository;
import com.wbozon.wb.service.ProductCardService;

public class WildberriesFacade {
    private final ProductCardService service;

    public WildberriesFacade(String token, String filename) {
        WildberriesCardClient client = new WildberriesCardClient(token);
        ProductCardRepository repo = new ProductCardRepository(filename);
        this.service = new ProductCardService(client, repo);
    }

    public void update() {
        Instant updatedAfter = Instant.now().minus(Duration.ofHours(24));
        service.updateCardsFromWB(updatedAfter);
    }

    public List<ProductCard> getLocalCards() {
        return service.loadLocalCards();
    }
}
