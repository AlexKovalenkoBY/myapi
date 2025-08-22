package com.wbozon.wb.api.classes;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
    // Ответ API с ценами
    public class PriceApiResponse {
      @SerializedName("data")
      PriceData data;
      @SerializedName("error")
      boolean error;
      @SerializedName("errorText")
      String errorText;
  }
