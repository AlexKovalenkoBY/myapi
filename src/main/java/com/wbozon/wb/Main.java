package com.wbozon.wb;

import com.wbozon.wb.facade.WildberriesFacade;

public class Main {
   public static void main(String[] args) {
       String token = System.getenv("WB_TOKEN");
       new WildberriesFacade(token).update();
   }
}
