package com.wbozon.wb.service;

import com.wbozon.wb.client.WildberriesApiClient;

public class WildberriesProductService {
    private final WildberriesApiClient apiClient;

    public WildberriesProductService(WildberriesApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void updateWarehouses() {
        try {
            String json = apiClient.fetchWarehouses();
            System.out.println("Warehouses: " + json);
        } catch (Exception e) {
            System.err.println("Ошибка при получении складов: " + e.getMessage());
        }
    }
}
