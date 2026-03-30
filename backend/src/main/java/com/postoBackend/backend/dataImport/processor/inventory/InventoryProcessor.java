package com.postoBackend.backend.dataImport.processor.inventory;

import com.postoBackend.backend.dataImport.context.ImportContext;
import com.postoBackend.backend.dataImport.processor.ImportProcessor;
import com.postoBackend.backend.dataImport.reader.CsvReader;
import com.postoBackend.backend.domain.inventory.Inventory;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.service.InventoryUpsertService;
import com.postoBackend.backend.service.ProductLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class InventoryProcessor implements ImportProcessor {

    private static final Logger logger = LoggerFactory.getLogger(InventoryProcessor.class);
    private static final int MAX_SKIP_SAMPLES = 5;

    private final CsvReader csvReader;
    private final InventoryMapper inventoryMapper;
    private final ProductLookupService productLookupService;
    private final InventoryUpsertService inventoryUpsertService;

    public InventoryProcessor(
            CsvReader csvReader,
            InventoryMapper inventoryMapper,
            ProductLookupService productLookupService,
            InventoryUpsertService inventoryUpsertService
    ) {
        this.csvReader = csvReader;
        this.inventoryMapper = inventoryMapper;
        this.productLookupService = productLookupService;
        this.inventoryUpsertService = inventoryUpsertService;
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public void process(ImportContext context) {
        List<String[]> rows = csvReader.read(context.getFilePath());
        InventoryMapper.InventoryColumns currentColumns = null;
        int processedCount = 0;
        int skippedMissingProductCount = 0;
        int skippedConflictCount = 0;
        List<String> skipSamples = new ArrayList<>();

        for (String[] row : rows) {
            InventoryMapper.InventoryColumns detectedColumns = inventoryMapper.detectColumns(row);

            if (detectedColumns != null) {
                currentColumns = detectedColumns;
                continue;
            }

            if (currentColumns == null || !inventoryMapper.isInventoryRow(row, currentColumns)) {
                continue;
            }

            InventoryImportRow inventoryImportRow = inventoryMapper.map(row, currentColumns);
            Optional<Product> product;

            try {
                product = productLookupService.findProduct(
                        inventoryImportRow.productCode(),
                        inventoryImportRow.productBarcode()
                );
            } catch (IllegalStateException exception) {
                skippedConflictCount++;
                addSkipSample(skipSamples, inventoryImportRow, exception.getMessage());
                continue;
            }

            if (product.isEmpty()) {
                skippedMissingProductCount++;
                addSkipSample(
                        skipSamples,
                        inventoryImportRow,
                        "Product not found for inventory row"
                );
                continue;
            }

            Inventory inventory = inventoryImportRow.toInventory(product.get());

            inventoryUpsertService.saveOrUpdate(inventory);
            processedCount++;
        }

        logger.info(
                "Processed {} inventory rows from '{}' (skipped missing products: {}, skipped conflicting matches: {})",
                processedCount,
                context.getFileName(),
                skippedMissingProductCount,
                skippedConflictCount
        );

        if (!skipSamples.isEmpty()) {
            logger.warn(
                    "Inventory import skipped sample rows from '{}': {}",
                    context.getFileName(),
                    skipSamples
            );
        }
    }

    private void addSkipSample(
            List<String> skipSamples,
            InventoryImportRow row,
            String reason
    ) {
        if (skipSamples.size() >= MAX_SKIP_SAMPLES) {
            return;
        }

        skipSamples.add(
                reason
                        + " [code=" + normalizeLabel(row.productCode())
                        + ", barcode=" + normalizeLabel(row.productBarcode()) + "]"
        );
    }

    private String normalizeLabel(String value) {
        return value == null || value.isBlank() ? "<empty>" : value.trim();
    }
}
