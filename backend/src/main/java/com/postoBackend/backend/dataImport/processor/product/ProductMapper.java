package com.postoBackend.backend.dataImport.processor.product;

import com.postoBackend.backend.domain.product.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductMapper {

    private static final int CODE_INDEX = 4;
    private static final int BARCODE_INDEX = 5;
    private static final int NAME_INDEX = 6;
    private static final int PRICE_INDEX = 8;

    public Product map(String[] row) {
        String code = readCell(row, CODE_INDEX);
        String name = readCell(row, NAME_INDEX);
        String barcode = readCell(row, BARCODE_INDEX);
        BigDecimal price = parseDecimal(readCell(row, PRICE_INDEX), "price", true);

        return new Product(code, name, price, null, null, null, barcode);
    }

    private String readCell(String[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return null;
        }

        return row[index].trim();
    }

    private BigDecimal parseDecimal(String value, String field, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new IllegalArgumentException("Missing required " + field + " value");
            }
            return null;
        }

        try {
            String normalized = value
                    .replace(".", "")
                    .replace(",", ".");
            return new BigDecimal(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid " + field + " value: " + value, e);
        }
    }
}
