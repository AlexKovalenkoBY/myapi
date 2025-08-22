package com.wbozon.wb.api.classes;
import com.google.gson.annotations.SerializedName;
import lombok.*;
@Getter
@Setter
    // Габариты товара
    public class Dimensions {
      @SerializedName("width")
      Integer width;
      @SerializedName("height")
      Integer height;
      @SerializedName("length")
      Integer length;
      @SerializedName("weightBrutto")
      Double weightBrutto;
      @SerializedName("isValid")
      Boolean isValid;
  }