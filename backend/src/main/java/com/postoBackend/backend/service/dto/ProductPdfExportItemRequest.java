package com.postoBackend.backend.service.dto;

import java.math.BigDecimal;

public record ProductPdfExportItemRequest(
        String code,
        String name,
        BigDecimal price,
        BigDecimal cost,
        BigDecimal profitMargin,
        String category,
        String barcode
) {
}
