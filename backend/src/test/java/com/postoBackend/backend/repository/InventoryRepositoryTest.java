package com.postoBackend.backend.repository;

import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.inventory.Inventory;
import com.postoBackend.backend.domain.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void rejectsDuplicateInventoryForSameProduct() {
        Product product = persistProduct("2552", "GASOLINA ORIGINAL C", null);

        inventoryRepository.saveAndFlush(new Inventory(product, new BigDecimal("7165.686")));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inventoryRepository.saveAndFlush(new Inventory(product, new BigDecimal("6000.000")))
        );
    }

    @Test
    void findsInventoryByProductId() {
        Product product = persistProduct("2552", "GASOLINA ORIGINAL C", null);

        Inventory savedInventory = inventoryRepository.saveAndFlush(new Inventory(
                product,
                new BigDecimal("7165.686")
        ));

        assertEquals(1, inventoryRepository.count());
        assertTrue(inventoryRepository.findByProduct_Id(product.getId())
                .filter(foundInventory -> foundInventory.getId().equals(savedInventory.getId()))
                .isPresent());
    }

    private Product persistProduct(String code, String name, String barcode) {
        Category category = categoryRepository.saveAndFlush(new Category("1.1.", "COMBUSTIVEIS"));

        return productRepository.saveAndFlush(new Product(
                code,
                name,
                new BigDecimal("6.49"),
                new BigDecimal("5.75"),
                new BigDecimal("11.44"),
                category,
                barcode
        ));
    }
}
