package com.postoBackend.backend.dataImport.processor.inventory;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class InventoryMapper {

    private static final Pattern PRODUCT_CODE_PATTERN = Pattern.compile("^\\d+$");

    private static final String CODE_HEADER = "COD";
    private static final String PRODUCT_HEADER = "PRODUTO";
    private static final String BARCODE_HEADER = "CODIGO BARRA";
    private static final String QUANTITY_HEADER = "SALDO QTD";

    public InventoryColumns detectColumns(String[] row) {
        if (row == null) {
            return null;
        }

        Integer productCodeIndex = null;
        Integer productNameIndex = null;
        Integer productBarcodeIndex = null;
        Integer quantityIndex = null;

        for (int index = 0; index < row.length; index++) {
            String header = normalizeHeader(row[index]);

            if (header == null) {
                continue;
            }

            if (CODE_HEADER.equals(header)) {
                productCodeIndex = index;
                continue;
            }

            if (PRODUCT_HEADER.equals(header)) {
                productNameIndex = index;
                continue;
            }

            if (BARCODE_HEADER.equals(header)) {
                productBarcodeIndex = index;
                continue;
            }

            if (QUANTITY_HEADER.equals(header)) {
                quantityIndex = index;
            }
        }

        if (productCodeIndex == null || productNameIndex == null || quantityIndex == null) {
            return null;
        }

        return new InventoryColumns(
                productCodeIndex,
                productNameIndex,
                productBarcodeIndex != null ? productBarcodeIndex : -1,
                quantityIndex
        );
    }

    public boolean isInventoryRow(String[] row, InventoryColumns columns) {
        String productCode = getCell(row, columns.productCodeIndex());
        String productName = getCell(row, columns.productNameIndex());

        return productCode != null
                && PRODUCT_CODE_PATTERN.matcher(productCode).matches()
                && productName != null;
    }

    public InventoryImportRow map(String[] row, InventoryColumns columns) {
        return new InventoryImportRow(
                getCell(row, columns.productCodeIndex()),
                columns.productBarcodeIndex() >= 0 ? getCell(row, columns.productBarcodeIndex()) : null,
                parseDecimal(getCell(row, columns.quantityIndex()), "quantityInStock")
        );
    }

    private BigDecimal parseDecimal(String value, String field) {
        if (value == null) {
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

    private String getCell(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length) {
            return null;
        }

        String value = row[index];

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeHeader(String value) {
        String normalizedValue = normalizeValue(value);

        if (normalizedValue == null) {
            return null;
        }

        return Normalizer.normalize(normalizedValue, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeValue(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record InventoryColumns(
            int productCodeIndex,
            int productNameIndex,
            int productBarcodeIndex,
            int quantityIndex
    ) {
    }
}
