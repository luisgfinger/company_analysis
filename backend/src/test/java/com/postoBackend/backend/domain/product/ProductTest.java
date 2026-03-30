package com.postoBackend.backend.domain.product;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductTest {

    @Test
    void calculatesProfitMarginFromPriceAndCost() {
        Product product = new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                null,
                null
        );

        assertEquals(new BigDecimal("21.46"), product.getProfitMargin());
    }

    @Test
    void returnsNullProfitMarginWhenCostIsNull() {
        Product product = new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                null,
                new BigDecimal("21.51"),
                null,
                null
        );

        assertNull(product.getProfitMargin());
    }
}
