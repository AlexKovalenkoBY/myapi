package com.wbozon.wb;

import java.util.List;

import com.wbozon.wb.api.classes.ProductCard;
import com.wbozon.wb.facade.WildberriesFacade;

public class Main {
   public static void main(String[] args) {
       String token = System.getenv("WB_TOKEN");
       WildberriesFacade facade = new WildberriesFacade(token, "product_cards.json");

       facade.update();
       List<ProductCard> cards = facade.getLocalCards();
       cards.forEach(card -> System.out.println(card.getNmID()));
   }
}
