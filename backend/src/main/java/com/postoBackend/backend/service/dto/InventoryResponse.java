package com.postoBackend.backend.service.dto;

import com.postoBackend.backend.domain.inventory.Inventory;

import java.math.BigDecimal;

public record InventoryResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        String productBarcode,
        String category,
        BigDecimal quantityInStock
) {

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getProductCode(),
                inventory.getProductName(),
                inventory.getProductBarcode(),
                inventory.getProductCategoryName(),
                inventory.getQuantityInStock()
        );
    }
}
