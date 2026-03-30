package com.postoBackend.backend.service.dto;

public record ProductSearchCriteria(
        String q,
        String category,
        String code,
        String barcode
) {
}
