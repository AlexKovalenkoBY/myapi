package com.wbozon.wb.api.classes;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
 // Склад
public class WareHouseEntity {
   @SerializedName("id")
   Long id;
   @SerializedName("name")
   String name;
}
