package com.postoBackend.backend.dataImport.processor.product;


import org.springframework.stereotype.Component;

@Component
public class ProductRowFilter {

    private static final int CODE_INDEX = 4;
    private static final int NAME_INDEX = 6;
    private static final int PRICE_INDEX = 8;
    private static final int EXPECTED_MIN_COLUMNS = PRICE_INDEX + 1;

    public boolean isRelevant(String[] row) {
        if (row == null) {
            return false;
        }

        if (row.length < EXPECTED_MIN_COLUMNS) {
            return false;
        }

        if (isHeader(row)) {
            return false;
        }

        String code = row[CODE_INDEX];
        String name = row[NAME_INDEX];
        String price = row[PRICE_INDEX];

        return isNotBlank(code)
                && isNotBlank(name)
                && isNotBlank(price);
    }

    private boolean isHeader(String[] row) {
        return "movimento".equalsIgnoreCase(row[0].trim())
                || "codigo".equalsIgnoreCase(row[0].trim())
                || "code".equalsIgnoreCase(row[0].trim());
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
