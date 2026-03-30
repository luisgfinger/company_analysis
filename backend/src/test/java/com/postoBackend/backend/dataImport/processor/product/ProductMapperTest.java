package com.postoBackend.backend.dataImport.processor.product;

import com.postoBackend.backend.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductMapperTest {

    private final ProductMapper productMapper = new ProductMapper();

    @Test
    void mapsPriceWithoutApplyingProductsProfitMarginColumn() {
        String[] row = {
                "Grupo",
                "A Vista",
                "1.1.",
                " COMBUSTIVEIS ",
                "2171",
                "123",
                "DIESEL S10",
                "27101921",
                "7,69",
                "0,00",
                "6,04",
                "21,51"
        };

        Product product = productMapper.map(row);

        assertEquals("2171", product.getCode());
        assertNull(product.getCategory());
        assertEquals(new BigDecimal("7.69"), product.getPrice());
        assertNull(product.getCost());
        assertNull(product.getProfitMargin());
    }

    @Test
    void leavesProfitMarginNullWhenColumnIsMissing() {
        String[] row = {
                "Grupo",
                "A Vista",
                "1.1.",
                "COMBUSTIVEIS",
                "2171",
                "123",
                "DIESEL S10",
                "27101921",
                "7,69",
                "0,00",
                "6,04"
        };

        Product product = productMapper.map(row);

        assertNull(product.getCost());
        assertNull(product.getProfitMargin());
    }
}
