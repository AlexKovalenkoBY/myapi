package com.wbozon.wb.api.classes;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cursor {
    private static final int GOODS_PER_REQUEST = 100;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    @SerializedName("nmID")
    private Long nmID;
    
    @SerializedName("total")
    private Integer total;
    
    @SerializedName("limit")
    private Integer limit = GOODS_PER_REQUEST;

    // Конструктор для обновлений по времени
    public Cursor(String updatedAt) {
        this.updatedAt = updatedAt;
        this.nmID = null;
        this.total = null;
        this.limit = GOODS_PER_REQUEST;
    }

    public String getCursor() {
        return this.updatedAt;
    }
}