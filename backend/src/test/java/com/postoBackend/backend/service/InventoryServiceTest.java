package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.inventory.Inventory;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.repository.InventoryRepository;
import com.postoBackend.backend.service.dto.InventoryResponse;
import com.postoBackend.backend.service.dto.InventorySearchCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void findPagedMapsRepositoryPage() {
        Inventory inventory = inventoryWithId(
                1L,
                productWithId(1L, "2552", "GASOLINA ORIGINAL C", null),
                new BigDecimal("7165.686")
        );
        PageRequest pageable = PageRequest.of(0, 5, Sort.by("product.name"));
        Page<Inventory> page = new PageImpl<>(List.of(inventory), pageable, 1);

        when(inventoryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<InventoryResponse> response = inventoryService.findPaged(
                new InventorySearchCriteria("gasolina", null, null, null),
                pageable
        );

        assertEquals(1, response.getTotalElements());
        assertEquals("2552", response.getContent().get(0).productCode());
        assertEquals(new BigDecimal("7165.686"), response.getContent().get(0).quantityInStock());
    }

    @Test
    void findAllUsesDefaultSortWhenSortIsUnsorted() {
        Inventory inventory = inventoryWithId(
                1L,
                productWithId(1L, "2552", "GASOLINA ORIGINAL C", "123"),
                new BigDecimal("7165.686")
        );
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        when(inventoryRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(inventory));

        List<InventoryResponse> response = inventoryService.findAll(
                new InventorySearchCriteria(null, null, null, null),
                Sort.unsorted()
        );

        verify(inventoryRepository).findAll(any(Specification.class), sortCaptor.capture());

        assertEquals("product.name: ASC,id: ASC", sortCaptor.getValue().toString());
        assertEquals(1, response.size());
        assertEquals("123", response.get(0).productBarcode());
        assertEquals(new BigDecimal("7165.686"), response.get(0).quantityInStock());
    }

    @Test
    void findByIdThrowsNotFoundWhenInventoryDoesNotExist() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inventoryService.findById(999L)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Inventory not found for id 999", exception.getReason());
    }

    private Product productWithId(Long id, String code, String name, String barcode) {
        Product product = new Product(
                code,
                name,
                new BigDecimal("6.49"),
                new BigDecimal("5.75"),
                new BigDecimal("11.44"),
                new Category("1.1.", "COMBUSTIVEIS"),
                barcode
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Inventory inventoryWithId(
            Long id,
            Product product,
            BigDecimal quantityInStock
    ) {
        Inventory inventory = new Inventory(
                product,
                quantityInStock
        );
        ReflectionTestUtils.setField(inventory, "id", id);
        return inventory;
    }
}
