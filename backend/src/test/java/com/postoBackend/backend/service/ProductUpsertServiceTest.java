package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductUpsertServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductUpsertService productUpsertService;

    @Test
    void savesNewProductWithoutApplyingIncomingCost() {
        Product incoming = new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                category("1.1.", "COMBUSTIVEIS"),
                "123"
        );

        when(productRepository.findByCode("2171")).thenReturn(Optional.empty());
        when(productRepository.findByBarcode("123")).thenReturn(Optional.empty());
        when(productRepository.save(incoming)).thenReturn(incoming);

        Product saved = productUpsertService.saveOrUpdate(incoming);

        assertSame(incoming, saved);
        assertNull(incoming.getCost());
        assertNull(incoming.getProfitMargin());
        verify(productRepository).save(incoming);
    }

    @Test
    void updatesExistingProductWhenCodeAlreadyExistsWithoutReplacingStoredCost() {
        Product existing = productWithId(
                1L,
                "2171",
                "DIESEL ANTIGO",
                new BigDecimal("7.10"),
                new BigDecimal("5.40"),
                new BigDecimal("18.00"),
                category("1.1.", "COMBUSTIVEIS"),
                "123"
        );
        Product incoming = new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                category("1.2.", "ADITIVADO"),
                null
        );

        when(productRepository.findByCode("2171")).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        Product saved = productUpsertService.saveOrUpdate(incoming);

        assertSame(existing, saved);
        assertEquals("DIESEL S10", existing.getName());
        assertEquals(new BigDecimal("7.69"), existing.getPrice());
        assertEquals(new BigDecimal("5.40"), existing.getCost());
        assertEquals(new BigDecimal("29.78"), existing.getProfitMargin());
        assertEquals("ADITIVADO", existing.getCategory().getName());
        assertEquals("123", existing.getBarcode());
        verify(productRepository, never()).findByBarcode(any());
        verify(productRepository).save(existing);
    }

    @Test
    void updatesExistingProductWhenBarcodeAlreadyExistsWithoutReplacingStoredCost() {
        Product existing = productWithId(
                2L,
                "OLD-001",
                "OLEO",
                new BigDecimal("10.00"),
                new BigDecimal("8.50"),
                new BigDecimal("10.00"),
                category("2.1.", "LUBRIFICANTES"),
                "789"
        );
        Product incoming = new Product(
                "NEW-001",
                "OLEO PREMIUM",
                new BigDecimal("12.50"),
                new BigDecimal("10.85"),
                new BigDecimal("15.25"),
                category("2.1.", "LUBRIFICANTES"),
                "789"
        );

        when(productRepository.findByCode("NEW-001")).thenReturn(Optional.empty());
        when(productRepository.findByBarcode("789")).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        Product saved = productUpsertService.saveOrUpdate(incoming);

        assertSame(existing, saved);
        assertEquals("NEW-001", existing.getCode());
        assertEquals("OLEO PREMIUM", existing.getName());
        assertEquals(new BigDecimal("12.50"), existing.getPrice());
        assertEquals(new BigDecimal("8.50"), existing.getCost());
        assertEquals(new BigDecimal("32.00"), existing.getProfitMargin());
        assertEquals("789", existing.getBarcode());
        verify(productRepository).save(existing);
    }

    @Test
    void throwsWhenCodeAndBarcodePointToDifferentProducts() {
        Product byCode = productWithId(
                1L,
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                category("1.1.", "COMBUSTIVEIS"),
                "123"
        );
        Product byBarcode = productWithId(
                2L,
                "3000",
                "GASOLINA",
                new BigDecimal("6.49"),
                new BigDecimal("5.80"),
                new BigDecimal("12.00"),
                category("1.1.", "COMBUSTIVEIS"),
                "999"
        );
        Product incoming = new Product(
                "2171",
                "NOVO",
                new BigDecimal("7.99"),
                new BigDecimal("6.50"),
                new BigDecimal("19.75"),
                category("1.1.", "COMBUSTIVEIS"),
                "999"
        );

        when(productRepository.findByCode("2171")).thenReturn(Optional.of(byCode));
        when(productRepository.findByBarcode("999")).thenReturn(Optional.of(byBarcode));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productUpsertService.saveOrUpdate(incoming)
        );

        assertEquals("Conflicting products found for code '2171' and barcode '999'", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void normalizesBlankBarcodeToNullOnNewProduct() {
        Product incoming = new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                category("1.1.", "COMBUSTIVEIS"),
                "   "
        );

        when(productRepository.findByCode("2171")).thenReturn(Optional.empty());
        when(productRepository.save(incoming)).thenReturn(incoming);

        Product saved = productUpsertService.saveOrUpdate(incoming);

        assertSame(incoming, saved);
        assertNull(incoming.getCost());
        assertNull(incoming.getProfitMargin());
        assertNull(incoming.getBarcode());
        verify(productRepository, never()).findByBarcode(any());
    }

    @Test
    void keepsExistingCostAndRecalculatesMarginWhenIncomingValuesAreMissing() {
        Product existing = productWithId(
                1L,
                "2171",
                "DIESEL ANTIGO",
                new BigDecimal("7.10"),
                new BigDecimal("5.40"),
                new BigDecimal("18.00"),
                category("1.1.", "COMBUSTIVEIS"),
                "123"
        );
        Product incoming = new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                null,
                null,
                category("1.2.", "ADITIVADO"),
                null
        );

        when(productRepository.findByCode("2171")).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        Product saved = productUpsertService.saveOrUpdate(incoming);

        assertSame(existing, saved);
        assertEquals(new BigDecimal("5.40"), existing.getCost());
        assertEquals(new BigDecimal("29.78"), existing.getProfitMargin());
        assertEquals("ADITIVADO", existing.getCategory().getName());
    }

    @Test
    void updatesOnlyCostWhenRequested() {
        Product existing = productWithId(
                1L,
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("5.40"),
                new BigDecimal("21.51"),
                category("1.1.", "COMBUSTIVEIS"),
                "123"
        );

        when(productRepository.save(existing)).thenReturn(existing);

        Product saved = productUpsertService.updateCost(existing, new BigDecimal("5.99"));

        assertSame(existing, saved);
        assertEquals(new BigDecimal("5.99"), existing.getCost());
        assertEquals(new BigDecimal("22.11"), existing.getProfitMargin());
        verify(productRepository).save(existing);
    }

    private Product productWithId(
            Long id,
            String code,
            String name,
            BigDecimal price,
            BigDecimal cost,
            BigDecimal profitMargin,
            Category category,
            String barcode
    ) {
        Product product = new Product(code, name, price, cost, profitMargin, category, barcode);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Category category(String code, String name) {
        return new Category(code, name);
    }
}
