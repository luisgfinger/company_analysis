package com.postoBackend.backend.dataImport.processor;

import com.postoBackend.backend.dataImport.context.ImportContext;
import com.postoBackend.backend.dataImport.processor.inventory.InventoryProcessor;
import com.postoBackend.backend.dataImport.processor.purchasing.PurchasingProcessor;
import com.postoBackend.backend.dataImport.processor.product.ProductProcessor;
import org.springframework.stereotype.Component;

@Component
public class ImportProcessorResolver {

    private final ProductProcessor productProcessor;
    private final PurchasingProcessor purchasingProcessor;
    private final InventoryProcessor inventoryProcessor;

    public ImportProcessorResolver(
            ProductProcessor productProcessor,
            PurchasingProcessor purchasingProcessor,
            InventoryProcessor inventoryProcessor
    ) {
        this.productProcessor = productProcessor;
        this.purchasingProcessor = purchasingProcessor;
        this.inventoryProcessor = inventoryProcessor;
    }

    public ImportProcessor resolve(ImportContext context) {
        String module = context.getModule();

        if ("products".equalsIgnoreCase(module)) {
            return productProcessor;
        }

        if ("inventory".equalsIgnoreCase(module)) {
            return inventoryProcessor;
        }

        if ("purchasing".equalsIgnoreCase(module)) {
            return purchasingProcessor;
        }

        throw new RuntimeException("No processor found for module: " + module);
    }
}
