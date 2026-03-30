package com.postoBackend.backend.controller;

import com.postoBackend.backend.service.ProductPdfExportService;
import com.postoBackend.backend.service.ProductService;
import com.postoBackend.backend.service.dto.ProductPdfExportRequest;
import com.postoBackend.backend.service.dto.ProductPdfExportResult;
import com.postoBackend.backend.service.dto.ProductResponse;
import com.postoBackend.backend.service.dto.ProductSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductPdfExportService productPdfExportService;

    public ProductController(ProductService productService, ProductPdfExportService productPdfExportService) {
        this.productService = productService;
        this.productPdfExportService = productPdfExportService;
    }

    @GetMapping
    public Page<ProductResponse> findPaged(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String barcode,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return productService.findPaged(new ProductSearchCriteria(q, category, code, barcode), pageable);
    }

    @GetMapping("/all")
    public List<ProductResponse> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String barcode,
            Sort sort
    ) {
        return productService.findAll(new ProductSearchCriteria(q, category, code, barcode), sort);
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping(path = "/export/pdf", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(@RequestBody ProductPdfExportRequest request) {
        ProductPdfExportResult report = productPdfExportService.export(request);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + buildFilename(report.title()) + "\"")
                .body(report.content());
    }

    private String buildFilename(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");

        if (normalized.isBlank()) {
            return "relatorio-produtos.pdf";
        }

        return normalized + ".pdf";
    }
}
