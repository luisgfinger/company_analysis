package com.postoBackend.backend.service.dto;

public record ProductPdfExportResult(
        String title,
        byte[] content
) {
}
