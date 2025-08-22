package com.wbozon.wb.api.classes;
import com.google.gson.annotations.SerializedName;
    // Тег товара
    public class Tag {
      @SerializedName("id")
      Long id;
      @SerializedName("name")
      String name;
      @SerializedName("color")
      String color;
  }
