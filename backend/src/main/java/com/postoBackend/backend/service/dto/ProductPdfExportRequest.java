package com.postoBackend.backend.service.dto;

import java.util.List;

public record ProductPdfExportRequest(
        String title,
        List<ProductPdfExportItemRequest> products
) {
}
