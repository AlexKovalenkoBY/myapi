package com.wbozon.wb.api.classes;
import com.google.gson.annotations.SerializedName;

import lombok.*;

@Getter
@Setter
    // Размер товара
    public class WbSize {
      @SerializedName("sizeID")
      long sizeID;
      @SerializedName("price")
      double price;
      @SerializedName("discountedPrice")
      double discountedPrice;
      @SerializedName("clubDiscountedPrice")
      double clubDiscountedPrice;
      @SerializedName("techSizeName")
      String techSizeName;
      @SerializedName("skus")
      
      String[] skus;
  }

 
