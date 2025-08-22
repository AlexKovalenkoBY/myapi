package com.wbozon.wb.api.classes;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
public class StockEntity {
    // Остаток товара на складе
    @SerializedName("nmId")
    Long nmId;
    @SerializedName("sku")
    String sku;
    @SerializedName("amount")
    Integer amount;
    @SerializedName("warehouseId")
    Long warehouseId;
}
