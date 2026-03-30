package com.postoBackend.backend.dataImport.processor.inventory;

import com.postoBackend.backend.domain.inventory.Inventory;
import com.postoBackend.backend.domain.product.Product;

import java.math.BigDecimal;

public record InventoryImportRow(
        String productCode,
        String productBarcode,
        BigDecimal quantityInStock
) {

    public Inventory toInventory(Product product) {
        return new Inventory(
                product,
                quantityInStock
        );
    }
}
