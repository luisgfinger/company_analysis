package com.postoBackend.backend.dataImport.processor.purchasing;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class PurchasingMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PurchasingGroup detectGroup(String[] row) {
        String firstValue = firstValue(row);

        if (!StringUtils.hasText(firstValue) || !firstValue.startsWith("Grupo:")) {
            return null;
        }

        String value = firstValue.substring("Grupo:".length()).trim();
        int separatorIndex = value.indexOf(" - ");

        if (separatorIndex < 0) {
            return new PurchasingGroup(normalizeValue(value), null);
        }

        String categoryCode = normalizeValue(value.substring(0, separatorIndex));
        String categoryName = normalizeValue(value.substring(separatorIndex + 3));

        return new PurchasingGroup(categoryCode, categoryName);
    }

    public boolean isPurchaseRow(String[] row) {
        List<String> values = compactValues(row);

        if (values.size() < 5) {
            return false;
        }

        String firstValue = values.get(0);

        if (isNonDataLabel(firstValue)) {
            return false;
        }

        if (!isDate(values.get(1))) {
            return false;
        }

        return isDecimal(values.get(4));
    }

    public PurchasingImportRow map(String[] row, PurchasingGroup group) {
        List<String> values = compactValues(row);

        if (group == null || !StringUtils.hasText(group.categoryCode())) {
            throw new IllegalArgumentException("Missing purchasing group context");
        }

        if (values.size() < 5) {
            throw new IllegalArgumentException("Invalid purchasing row");
        }

        String document = values.size() > 2 ? values.get(2) : null;

        return new PurchasingImportRow(
                group.categoryCode(),
                group.categoryName(),
                values.get(0),
                parseDate(values.get(1)),
                document,
                parseDecimal(values.get(4), "cost")
        );
    }

    private List<String> compactValues(String[] row) {
        List<String> values = new ArrayList<>();

        if (row == null) {
            return values;
        }

        for (String cell : row) {
            String normalized = normalizeValue(cell);

            if (normalized != null) {
                values.add(normalized);
            }
        }

        return values;
    }

    private String firstValue(String[] row) {
        List<String> values = compactValues(row);
        return values.isEmpty() ? null : values.get(0);
    }

    private boolean isNonDataLabel(String value) {
        if (!StringUtils.hasText(value)) {
            return true;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        return normalized.startsWith("FORNECEDOR:")
                || normalized.startsWith("TOTAL FORNECEDOR:")
                || normalized.startsWith("TOTAL GRUPO:")
                || normalized.startsWith("TOTAL GERAL:")
                || normalized.startsWith("SISTEMA SIGILO")
                || normalized.startsWith("ENTRADAQTDE TOTAL")
                || normalized.startsWith("PRODUTO")
                || normalized.startsWith("PAGINA:")
                || normalized.startsWith("AUTO POSTO")
                || normalized.startsWith("COMPRAS DE PRODUTOS")
                || normalized.startsWith("DE:");
    }

    private boolean isDate(String value) {
        try {
            parseDate(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid movementDate value: " + value, exception);
        }
    }

    private boolean isDecimal(String value) {
        try {
            parseDecimal(value, "cost");
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private BigDecimal parseDecimal(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Missing required " + field + " value");
        }

        try {
            String normalized = value
                    .replace(".", "")
                    .replace(",", ".");
            return new BigDecimal(normalized);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid " + field + " value: " + value, exception);
        }
    }

    private String normalizeValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record PurchasingGroup(String categoryCode, String categoryName) {
    }
}
