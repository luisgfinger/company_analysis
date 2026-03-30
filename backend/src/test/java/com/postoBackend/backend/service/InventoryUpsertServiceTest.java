package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.inventory.Inventory;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryUpsertServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryUpsertService inventoryUpsertService;

    @Test
    void savesNewInventoryWhenProductIsNew() {
        Product product = productWithId(1L, "2552", "GASOLINA ORIGINAL C");
        Inventory incomingInventory = new Inventory(
                product,
                new BigDecimal("7165.686")
        );

        when(inventoryRepository.findByProduct_Id(1L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(incomingInventory)).thenReturn(incomingInventory);

        Inventory savedInventory = inventoryUpsertService.saveOrUpdate(incomingInventory);

        assertSame(incomingInventory, savedInventory);
        verify(inventoryRepository).save(incomingInventory);
    }

    @Test
    void updatesExistingInventoryWhenProductAlreadyExists() {
        Product product = productWithId(1L, "2552", "GASOLINA ORIGINAL C");
        Inventory existingInventory = inventoryWithId(
                10L,
                product,
                new BigDecimal("6000.000")
        );
        Inventory incomingInventory = new Inventory(
                product,
                new BigDecimal("7165.686")
        );

        when(inventoryRepository.findByProduct_Id(1L)).thenReturn(Optional.of(existingInventory));
        when(inventoryRepository.save(existingInventory)).thenReturn(existingInventory);

        Inventory savedInventory = inventoryUpsertService.saveOrUpdate(incomingInventory);

        assertSame(existingInventory, savedInventory);
        assertEquals(new BigDecimal("7165.686"), existingInventory.getQuantityInStock());
        verify(inventoryRepository).save(existingInventory);
    }

    @Test
    void rejectsInventoryWithoutPersistedProduct() {
        Inventory incomingInventory = new Inventory(
                new Product(
                        "2552",
                        "GASOLINA ORIGINAL C",
                        new BigDecimal("6.49"),
                        new BigDecimal("5.75"),
                        new BigDecimal("11.44"),
                        new Category("1.1.", "COMBUSTIVEIS"),
                        null
                ),
                new BigDecimal("7165.686")
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryUpsertService.saveOrUpdate(incomingInventory)
        );

        assertEquals("Inventory must reference a persisted product", exception.getMessage());
    }

    private Product productWithId(Long id, String code, String name) {
        Product product = new Product(
                code,
                name,
                new BigDecimal("6.49"),
                new BigDecimal("5.75"),
                new BigDecimal("11.44"),
                new Category("1.1.", "COMBUSTIVEIS"),
                null
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
