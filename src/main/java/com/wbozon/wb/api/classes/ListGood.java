package com.wbozon.wb.api.classes;

import com.google.gson.annotations.SerializedName;
import lombok.*;
// Информация о товаре в ответе ценового API

@Getter
@Setter
public class ListGood {
   @SerializedName("nmID")
   long nmID;
   @SerializedName("vendorCode")
   String vendorCode;
   @SerializedName("sizes")
   WbSize[] sizes;
   @SerializedName("currencyIsoCode4217")
   String currencyIsoCode4217;
   @SerializedName("discount")
   long discount;
   @SerializedName("clubDiscount")
   long clubDiscount;
   @SerializedName("editableSizePrice")
   boolean editableSizePrice;
}
