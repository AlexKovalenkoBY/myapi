package com.wbozon.wb.api.classes;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
// Ответ API с карточками товаров
public class ApiResponse {
  @SerializedName("cards")
  List<ProductCard> cards;
  @SerializedName("cursor")
  Cursor cursor;
}
