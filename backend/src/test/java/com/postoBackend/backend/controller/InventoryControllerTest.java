package com.postoBackend.backend.controller;

import com.postoBackend.backend.service.InventoryService;
import com.postoBackend.backend.service.dto.InventoryResponse;
import com.postoBackend.backend.service.dto.InventorySearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SortHandlerMethodArgumentResolver sortResolver = new SortHandlerMethodArgumentResolver();
        PageableHandlerMethodArgumentResolver pageableResolver =
                new PageableHandlerMethodArgumentResolver(sortResolver);

        mockMvc = MockMvcBuilders.standaloneSetup(new InventoryController(inventoryService))
                .setCustomArgumentResolvers(pageableResolver, sortResolver)
                .build();
    }

    @Test
    void findPagedReturnsPageAndForwardsFilters() throws Exception {
        InventoryResponse response = new InventoryResponse(
                1L,
                10L,
                "2552",
                "GASOLINA ORIGINAL C",
                null,
                "COMBUSTIVEIS",
                new BigDecimal("7165.686")
        );

        when(inventoryService.findPaged(any(InventorySearchCriteria.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 5, Sort.by("product.name")), 1));

        mockMvc.perform(get("/api/v1/inventory")
                        .param("q", "gasolina")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("GASOLINA ORIGINAL C"))
                .andExpect(jsonPath("$.content[0].quantityInStock").value(7165.686))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<InventorySearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(InventorySearchCriteria.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(inventoryService).findPaged(criteriaCaptor.capture(), pageableCaptor.capture());

        assertEquals("gasolina", criteriaCaptor.getValue().q());
        assertEquals(1, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void findAllReturnsListAndForwardsSort() throws Exception {
        InventoryResponse response = new InventoryResponse(
                1L,
                10L,
                "2552",
                "GASOLINA ORIGINAL C",
                "123",
                "COMBUSTIVEIS",
                new BigDecimal("7165.686")
        );

        when(inventoryService.findAll(any(InventorySearchCriteria.class), any(Sort.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/inventory/all")
                        .param("barcode", "123")
                        .param("sort", "quantityInStock,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productBarcode").value("123"))
                .andExpect(jsonPath("$[0].quantityInStock").value(7165.686));

        ArgumentCaptor<InventorySearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(InventorySearchCriteria.class);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        verify(inventoryService).findAll(criteriaCaptor.capture(), sortCaptor.capture());

        assertEquals("123", criteriaCaptor.getValue().barcode());
        assertEquals("quantityInStock: DESC", sortCaptor.getValue().toString());
    }

    @Test
    void findByIdReturnsSingleInventory() throws Exception {
        InventoryResponse response = new InventoryResponse(
                1L,
                10L,
                "2552",
                "GASOLINA ORIGINAL C",
                null,
                "COMBUSTIVEIS",
                new BigDecimal("7165.686")
        );

        when(inventoryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productCode").value("2552"))
                .andExpect(jsonPath("$.quantityInStock").value(7165.686));
    }
}
