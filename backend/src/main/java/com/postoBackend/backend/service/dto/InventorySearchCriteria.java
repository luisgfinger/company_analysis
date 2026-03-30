package com.postoBackend.backend.service.dto;

public record InventorySearchCriteria(
        String q,
        String code,
        String barcode,
        String category
) {
}
