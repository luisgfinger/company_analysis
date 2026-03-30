package com.postoBackend.backend.dataImport.processor.purchasing;

import com.postoBackend.backend.dataImport.context.ImportContext;
import com.postoBackend.backend.dataImport.processor.ImportProcessor;
import com.postoBackend.backend.dataImport.reader.CsvReader;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.service.ProductLookupService;
import com.postoBackend.backend.service.ProductUpsertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PurchasingProcessor implements ImportProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PurchasingProcessor.class);
    private static final int MAX_SKIP_SAMPLES = 5;

    private final CsvReader csvReader;
    private final PurchasingMapper purchasingMapper;
    private final ProductLookupService productLookupService;
    private final ProductUpsertService productUpsertService;

    public PurchasingProcessor(
            CsvReader csvReader,
            PurchasingMapper purchasingMapper,
            ProductLookupService productLookupService,
            ProductUpsertService productUpsertService
    ) {
        this.csvReader = csvReader;
        this.purchasingMapper = purchasingMapper;
        this.productLookupService = productLookupService;
        this.productUpsertService = productUpsertService;
    }

    @Override
    public int getOrder() {
        return 15;
    }

    @Override
    public void process(ImportContext context) {
        List<String[]> rows = csvReader.read(context.getFilePath());
        PurchasingMapper.PurchasingGroup currentGroup = null;
        Map<PurchasingProductKey, PurchasingImportRow> latestRowsByProduct = new LinkedHashMap<>();
        List<String> invalidSamples = new ArrayList<>();
        List<String> lookupSkipSamples = new ArrayList<>();
        int validRows = 0;
        int skippedInvalidRows = 0;
        int skippedMissingProductCount = 0;
        int skippedConflictCount = 0;
        int updatedProducts = 0;

        for (String[] row : rows) {
            PurchasingMapper.PurchasingGroup detectedGroup = purchasingMapper.detectGroup(row);

            if (detectedGroup != null) {
                currentGroup = detectedGroup;
                continue;
            }

            if (currentGroup == null || !purchasingMapper.isPurchaseRow(row)) {
                continue;
            }

            PurchasingImportRow importRow;

            try {
                importRow = purchasingMapper.map(row, currentGroup);
            } catch (IllegalArgumentException exception) {
                skippedInvalidRows++;
                addSkipSample(invalidSamples, firstValue(row), exception.getMessage());
                continue;
            }

            validRows++;
            PurchasingProductKey productKey = new PurchasingProductKey(
                    importRow.categoryCode(),
                    importRow.productName()
            );
            PurchasingImportRow existingRow = latestRowsByProduct.get(productKey);

            if (existingRow == null || !importRow.movementDate().isBefore(existingRow.movementDate())) {
                latestRowsByProduct.put(productKey, importRow);
            }
        }

        for (PurchasingImportRow importRow : latestRowsByProduct.values()) {
            Optional<Product> product;

            try {
                product = productLookupService.findProductByCategoryCodeAndName(
                        importRow.categoryCode(),
                        importRow.productName()
                );
            } catch (IllegalStateException exception) {
                skippedConflictCount++;
                addSkipSample(lookupSkipSamples, importRow.productName(), exception.getMessage());
                continue;
            }

            if (product.isEmpty()) {
                skippedMissingProductCount++;
                addSkipSample(
                        lookupSkipSamples,
                        importRow.productName(),
                        "Product not found for purchasing row"
                );
                continue;
            }

            productUpsertService.updateCost(product.get(), importRow.cost());
            updatedProducts++;
        }

        logger.info(
                "Processed {} purchasing rows from '{}' (latest products: {}, updated products: {}, skipped invalid rows: {}, skipped missing products: {}, skipped conflicting matches: {})",
                validRows,
                context.getFileName(),
                latestRowsByProduct.size(),
                updatedProducts,
                skippedInvalidRows,
                skippedMissingProductCount,
                skippedConflictCount
        );

        if (!invalidSamples.isEmpty()) {
            logger.warn(
                    "Purchasing import skipped invalid sample rows from '{}': {}",
                    context.getFileName(),
                    invalidSamples
            );
        }

        if (!lookupSkipSamples.isEmpty()) {
            logger.warn(
                    "Purchasing import skipped unresolved sample rows from '{}': {}",
                    context.getFileName(),
                    lookupSkipSamples
            );
        }
    }

    private void addSkipSample(List<String> samples, String productName, String reason) {
        if (samples.size() >= MAX_SKIP_SAMPLES) {
            return;
        }

        samples.add(reason + " [product=" + normalizeLabel(productName) + "]");
    }

    private String firstValue(String[] row) {
        if (row == null || row.length == 0) {
            return null;
        }

        for (String value : row) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return null;
    }

    private String normalizeLabel(String value) {
        return value == null || value.isBlank() ? "<empty>" : value.trim();
    }

    private record PurchasingProductKey(String categoryCode, String productName) {
    }
}
