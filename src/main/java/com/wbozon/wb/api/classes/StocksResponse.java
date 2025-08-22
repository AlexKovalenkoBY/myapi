package com.wbozon.wb.api.classes;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
    // Ответ API с остатками
    public class StocksResponse {
      @SerializedName("stocks")
      StockEntity[] stocks;
  }
