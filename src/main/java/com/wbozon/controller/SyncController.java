package com.wbozon.controller;



import com.wbozon.wb.api.classes.WareHouseEntity;
import com.wbozon.wb.service.ProductCardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final ProductCardService service;

    public SyncController(ProductCardService service) {
        this.service = service;
    }

    @PostMapping("/prices")
    public String syncPrices(@RequestHeader("Authorization") String token) {
        service.syncPricesAsync(token);
        return "✅ Синхронизация цен запущена";
    }

    @PostMapping("/stocks")
    public String syncStocks(@RequestHeader("Authorization") String token,
                             @RequestBody List<WareHouseEntity> warehouses) {
        service.syncStocksAsync(token, warehouses);
        return "✅ Синхронизация остатков запущена";
    }
}

