package com.wbozon.wb.api.classes;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
// Карточка товара
public class ProductCard {
  @SerializedName("nmID")
  public Long nmID;
  @SerializedName("imtID")
  Long imtID;
  @SerializedName("nmUUID")
  String nmUUID;
  @SerializedName("subjectID")
  Integer subjectID;
  @SerializedName("subjectName")
  String subjectName;
  @SerializedName("vendorCode")
  String vendorCode;
  @SerializedName("brand")
  String brand;
  @SerializedName("title")
  String title;
  @SerializedName("description")
  String description;
  @SerializedName("needKiz")
  Boolean needKiz;
  @SerializedName("photos")
  List<Photo> photos;
  @SerializedName("video")
  String video;
  @SerializedName("dimensions")
  Dimensions dimensions;
  @SerializedName("characteristics")
  List<Characteristic> characteristics;
  @SerializedName("sizes")
  List<WbSize> sizes;
  @SerializedName("tags")
  List<Tag> tags;
  @SerializedName("createdAt")
  String createdAt;
  @SerializedName("updatedAt")
  String updatedAt;
  @SerializedName("priceInfo")
  ListGood priceInfo;
  @SerializedName("stockInfo")
  ProductStockInfo stockInfo;
}
