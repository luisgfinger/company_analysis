package com.postoBackend.backend.controller;

import com.postoBackend.backend.service.InventoryService;
import com.postoBackend.backend.service.dto.InventoryResponse;
import com.postoBackend.backend.service.dto.InventorySearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public Page<InventoryResponse> findPaged(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20, sort = "product.name") Pageable pageable
    ) {
        return inventoryService.findPaged(
                new InventorySearchCriteria(q, code, barcode, category),
                pageable
        );
    }

    @GetMapping("/all")
    public List<InventoryResponse> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String category,
            Sort sort
    ) {
        return inventoryService.findAll(
                new InventorySearchCriteria(q, code, barcode, category),
                sort
        );
    }

    @GetMapping("/{id}")
    public InventoryResponse findById(@PathVariable Long id) {
        return inventoryService.findById(id);
    }
}
