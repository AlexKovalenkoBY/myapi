package com.wbozon.controller;



import com.wbozon.wb.api.classes.WareHouseEntity;
import com.wbozon.wb.service.ProductCardService;
import com.wbozon.wb.service.WildberriesProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final WildberriesProductService service;

    public SyncController(WildberriesProductService service) {
        this.service = service;
    }

    @PostMapping("/prices")
    public String syncPrices(@RequestHeader("Authorization") String token) {
        // service.syncPricesAsync(token);
        return "✅ Синхронизация цен запущена";
    }

    @PostMapping("/stocks")
    public String syncStocks(@RequestHeader("Authorization") String token,
                             @RequestBody List<WareHouseEntity> warehouses) {
        service.syncStocksAsync();
        return "✅ Синхронизация остатков запущена";
    }
}

