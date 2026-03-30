package com.postoBackend.backend.dataImport.processor.purchasing;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchasingImportRow(
        String categoryCode,
        String categoryName,
        String productName,
        LocalDate movementDate,
        String document,
        BigDecimal cost
) {
}
