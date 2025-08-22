package com.wbozon.wb.api.classes;

public class ProductPriceInfo {
   double price;
   double discountedPrice;
   double clubDiscountedPrice;
   String currency;

   public ProductPriceInfo(double price, double discountedPrice, double clubDiscountedPrice, String currency) {
       this.price = price;
       this.discountedPrice = discountedPrice;
       this.clubDiscountedPrice = clubDiscountedPrice;
       this.currency = currency;
   }
}