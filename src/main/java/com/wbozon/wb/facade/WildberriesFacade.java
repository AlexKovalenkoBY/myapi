package com.wbozon.wb.facade;

import com.wbozon.wb.client.WildberriesApiClient;
import com.wbozon.wb.service.WildberriesProductService;

public class WildberriesFacade {
    private final WildberriesProductService service;

    public WildberriesFacade(String token) {
        WildberriesApiClient client = new WildberriesApiClient(token);
        this.service = new WildberriesProductService(client);
    }

    public void update() {
        service.updateWarehouses();
    }
}
