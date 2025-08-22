package com.wbozon.wb.api.classes;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
    // Характеристика товара
    public class Characteristic {
      @SerializedName("id")
      Long id;
      @SerializedName("name")
      String name;
      @SerializedName("value")
      JsonElement value;
  }
