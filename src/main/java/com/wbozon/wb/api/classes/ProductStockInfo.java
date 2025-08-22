package com.wbozon.wb.api.classes;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
    // Информация об остатках товара
    public  class ProductStockInfo {
      @SerializedName("amount")
      public Integer amount;
      @SerializedName("sku")
      public String sku;

      public ProductStockInfo(Integer amount, String sku) {
          this.amount = amount;
          this.sku = sku;
      }
  }

