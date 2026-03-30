package com.postoBackend.backend.service;

import com.postoBackend.backend.service.dto.ProductPdfExportItemRequest;
import com.postoBackend.backend.service.dto.ProductPdfExportRequest;
import com.postoBackend.backend.service.dto.ProductPdfExportResult;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductPdfExportServiceTest {

    private final ProductPdfExportService productPdfExportService = new ProductPdfExportService();

    @Test
    void exportBuildsPdfWithProvidedTitleAndProducts() {
        ProductPdfExportRequest request = new ProductPdfExportRequest(
                "Relatorio Produtos",
                List.of(product("2171", "DIESEL S10", "123"))
        );

        ProductPdfExportResult result = productPdfExportService.export(request);
        String pdfContent = new String(result.content(), StandardCharsets.ISO_8859_1);

        assertEquals("Relatorio Produtos", result.title());
        assertTrue(result.content().length > 0);
        assertTrue(pdfContent.startsWith("%PDF-1.4"));
        assertTrue(pdfContent.contains("Relatorio Produtos"));
        assertTrue(pdfContent.contains("DIESEL S10"));
        assertTrue(pdfContent.contains("COMBUSTIVEIS"));
        assertTrue(pdfContent.contains("Custo"));
        assertTrue(pdfContent.contains("Preco"));
        assertTrue(pdfContent.contains("Margem"));
        assertTrue(pdfContent.contains("R$ 7,69"));
        assertTrue(pdfContent.contains("R$ 6,04"));
        assertTrue(pdfContent.contains("21,51%"));
        assertTrue(pdfContent.contains("440 28 Td\n(Pagina 1 de 1) Tj"));
        assertFalse(pdfContent.contains("1. DIESEL S10"));
        assertFalse(pdfContent.contains("Codigo: 2171"));
        assertFalse(pdfContent.contains("Categoria: COMBUSTIVEIS"));
        assertFalse(pdfContent.contains("Barcode: 123"));
    }

    @Test
    void exportUsesDefaultTitleWhenBlankAndAllowsEmptyList() {
        ProductPdfExportResult result = productPdfExportService.export(
                new ProductPdfExportRequest("   ", List.of())
        );
        String pdfContent = new String(result.content(), StandardCharsets.ISO_8859_1);

        assertEquals("Relatorio de Produtos", result.title());
        assertTrue(pdfContent.contains("Relatorio de Produtos"));
        assertTrue(pdfContent.contains("Nenhum produto informado para exportacao."));
    }

    @Test
    void exportRejectsMissingProductsList() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productPdfExportService.export(new ProductPdfExportRequest("Relatorio", null))
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Products list is required to export the report", exception.getReason());
    }

    @Test
    void exportRejectsNullItemInsideProductsList() {
        List<ProductPdfExportItemRequest> products = new ArrayList<>();
        products.add(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productPdfExportService.export(new ProductPdfExportRequest("Relatorio", products))
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Products list cannot contain null items", exception.getReason());
    }

    @Test
    void exportRendersTitleOnlyOnFirstPageWhenReportSpansMultiplePages() {
        List<ProductPdfExportItemRequest> products = new ArrayList<>();

        for (int index = 1; index <= 40; index++) {
            products.add(product("COD-" + index, "PRODUTO " + index, "BAR-" + index));
        }

        ProductPdfExportResult result = productPdfExportService.export(
                new ProductPdfExportRequest("Relatorio Produtos", products)
        );
        String pdfContent = new String(result.content(), StandardCharsets.ISO_8859_1);

        assertEquals(1, countOccurrences(pdfContent, "Relatorio Produtos"));
        assertEquals(1, countOccurrences(pdfContent, "Total de produtos: 40"));
        assertEquals(2, countOccurrences(pdfContent, "COMBUSTIVEIS"));
        assertTrue(pdfContent.contains("440 28 Td\n(Pagina 1 de 2) Tj"));
        assertTrue(pdfContent.contains("440 28 Td\n(Pagina 2 de 2) Tj"));
        assertTrue(pdfContent.contains("PRODUTO 40"));
        assertFalse(pdfContent.contains("40 764 Td\n(Pagina 1 de 2) Tj"));
        assertFalse(pdfContent.contains("450 804 Td\n(Pagina 2 de 2) Tj"));
    }

    private ProductPdfExportItemRequest product(String code, String name, String barcode) {
        return new ProductPdfExportItemRequest(
                code,
                name,
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                "COMBUSTIVEIS",
                barcode
        );
    }

    private int countOccurrences(String value, String token) {
        int count = 0;
        int start = 0;

        while ((start = value.indexOf(token, start)) >= 0) {
            count++;
            start += token.length();
        }

        return count;
    }
}
