package com.wbozon.wb.service;

import java.time.Instant;
import java.util.List;

import com.wbozon.wb.api.classes.ProductCard;
import com.wbozon.wb.client.WildberriesCardClient;
import com.wbozon.wb.repository.ProductCardRepository;

public class ProductCardService {
   private final WildberriesCardClient client;
   private final ProductCardRepository repository;

   public ProductCardService(WildberriesCardClient client, ProductCardRepository repository) {
       this.client = client;
       this.repository = repository;
   }

   public void updateCardsFromWB(Instant updatedAfter) {
       try {
        // List<ProductCard> existedCards = loadLocalCards();
           List<ProductCard> updatedCards = client.fetchUpdatedCards(updatedAfter);
        //    updatedCards.forEach(c->{});
           repository.save(updatedCards);
       } catch (Exception e) {
           System.err.println("Ошибка при обновлении карточек: " + e.getMessage());
       }
   }

   public List<ProductCard> loadLocalCards() {
       return repository.load();
   }
}
