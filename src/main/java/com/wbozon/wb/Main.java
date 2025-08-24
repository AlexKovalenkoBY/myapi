package com.wbozon.wb;

import java.time.Instant;
import java.util.List;
import io.github.cdimascio.dotenv.Dotenv;
import com.wbozon.wb.api.classes.ProductCard;
import com.wbozon.wb.facade.WildberriesFacade;

public class Main {
    public static void main(String[] args) {
        // String token = System.getenv("WB_TOKEN");
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("BY_TOKEN");
        WildberriesFacade facade = new WildberriesFacade(token, "product_cards.json");
        Instant startFrom =facade.getTimeStartForUpdateInstant();
        // Instant startFromZero =Instant.now().minus(startFrom());
        facade.update(startFrom);
        List<ProductCard> cards = facade.getLocalCards();
       // cards.forEach(card -> System.out.println(card.getNmID()));
    }
}
