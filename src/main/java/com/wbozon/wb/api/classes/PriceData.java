package com.wbozon.wb.api.classes;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
public class PriceData {
   @SerializedName("listGoods")
   ListGood[] listGoods;
}