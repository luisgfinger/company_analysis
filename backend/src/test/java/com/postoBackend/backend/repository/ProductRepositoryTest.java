package com.postoBackend.backend.repository;

import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void rejectsDuplicateCode() {
        Category category = persistCategory("1.1.", "COMBUSTIVEIS");

        productRepository.saveAndFlush(new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                category,
                "123"
        ));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> productRepository.saveAndFlush(
                        new Product(
                                "2171",
                                "DIESEL S10 NOVO",
                                new BigDecimal("7.79"),
                                new BigDecimal("6.61"),
                                new BigDecimal("18.00"),
                                category,
                                "456"
                        )
                )
        );
    }

    @Test
    void rejectsDuplicateBarcodeWhenPresent() {
        Category category = persistCategory("1.1.", "COMBUSTIVEIS");

        productRepository.saveAndFlush(new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                category,
                "123"
        ));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> productRepository.saveAndFlush(
                        new Product(
                                "3000",
                                "GASOLINA",
                                new BigDecimal("6.49"),
                                new BigDecimal("5.80"),
                                new BigDecimal("12.00"),
                                category,
                                "123"
                        )
                )
        );
    }

    @Test
    void allowsMultipleProductsWithoutBarcode() {
        Category category = persistCategory("1.1.", "COMBUSTIVEIS");

        productRepository.saveAndFlush(new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                category,
                null
        ));
        productRepository.saveAndFlush(new Product(
                "3000",
                "GASOLINA",
                new BigDecimal("6.49"),
                new BigDecimal("5.80"),
                new BigDecimal("12.00"),
                category,
                "   "
        ));

        assertEquals(2, productRepository.count());
    }

    @Test
    void mapsProductCostToCostColumnInsteadOfLegacyMediumCostColumn() {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PRODUCTS'",
                String.class
        );

        assertTrue(columns.contains("COST"));
        assertFalse(columns.contains("MEDIUM_COST"));
    }

    private Category persistCategory(String code, String name) {
        return categoryRepository.saveAndFlush(new Category(code, name));
    }
}
