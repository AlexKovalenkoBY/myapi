package com.wbozon.wb.api.classes;
import com.google.gson.annotations.SerializedName;
import lombok.*;

    // Фото товара
    @Getter
    @Setter
    public class Photo {
      @SerializedName("big")
      String big;
      @SerializedName("c246x328")
      String c246x328;
      @SerializedName("c516x688")
      String c516x688;
      @SerializedName("hq")
      String hq;
      @SerializedName("square")
      String square;
      @SerializedName("tm")
      String tm;
  }
