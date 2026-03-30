package com.postoBackend.backend.service.dto;

import com.postoBackend.backend.domain.product.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String code,
        String name,
        BigDecimal price,
        BigDecimal cost,
        BigDecimal profitMargin,
        String category,
        String barcode
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getPrice(),
                product.getCost(),
                product.getProfitMargin(),
                product.getCategoryName(),
                product.getBarcode()
        );
    }
}
