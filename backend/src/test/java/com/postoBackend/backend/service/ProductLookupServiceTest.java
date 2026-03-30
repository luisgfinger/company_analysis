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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductLookupServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductLookupService productLookupService;

    @Test
    void resolvesProductByCode() {
        Product product = productWithId(1L, "2552", "GASOLINA ORIGINAL C", "123");

        when(productRepository.findByCode("2552")).thenReturn(Optional.of(product));

        Product resolved = productLookupService.findProduct("2552", null).orElseThrow();

        assertSame(product, resolved);
    }

    @Test
    void returnsEmptyWhenProductCannotBeResolved() {
        when(productRepository.findByCode("2552")).thenReturn(Optional.empty());

        Optional<Product> resolved = productLookupService.findProduct("2552", null);

        assertEquals(Optional.empty(), resolved);
    }

    @Test
    void resolvesRequiredProductByCode() {
        Product product = productWithId(1L, "2552", "GASOLINA ORIGINAL C", "123");

        when(productRepository.findByCode("2552")).thenReturn(Optional.of(product));

        Product resolved = productLookupService.findRequiredProduct("2552", null);

        assertSame(product, resolved);
    }

    @Test
    void resolvesProductByBarcodeWhenCodeIsMissing() {
        Product product = productWithId(1L, "8181", "2 TEMPO STL IPIRANGA", "7891165011993");

        when(productRepository.findByBarcode("7891165011993")).thenReturn(Optional.of(product));

        Product resolved = productLookupService.findRequiredProduct(null, "7891165011993");

        assertSame(product, resolved);
    }

    @Test
    void throwsWhenCodeAndBarcodePointToDifferentProducts() {
        Product productByCode = productWithId(1L, "2552", "GASOLINA ORIGINAL C", "123");
        Product productByBarcode = productWithId(2L, "8181", "2 TEMPO STL IPIRANGA", "7891165011993");

        when(productRepository.findByCode("2552")).thenReturn(Optional.of(productByCode));
        when(productRepository.findByBarcode("7891165011993")).thenReturn(Optional.of(productByBarcode));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productLookupService.findRequiredProduct("2552", "7891165011993")
        );

        assertEquals(
                "Conflicting products found for inventory row with code '2552' and barcode '7891165011993'",
                exception.getMessage()
        );
    }

    @Test
    void throwsWhenProductCannotBeResolved() {
        when(productRepository.findByCode("2552")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productLookupService.findRequiredProduct("2552", null)
        );

        assertEquals(
                "Product not found for inventory row with code '2552' and barcode '<empty>'",
                exception.getMessage()
        );
    }

    @Test
    void resolvesProductByCategoryCodeAndName() {
        Product product = productWithId(1L, "2552", "GASOLINA ORIGINAL C", "123");

        when(productRepository.findAllByCategory_CodeAndName("1.1.", "GASOLINA ORIGINAL C"))
                .thenReturn(List.of(product));

        Product resolved = productLookupService.findProductByCategoryCodeAndName(
                "1.1.",
                "GASOLINA ORIGINAL C"
        ).orElseThrow();

        assertSame(product, resolved);
    }

    @Test
    void resolvesProductByCategoryCodeAndNameUsingNormalizedFallback() {
        Product product = productWithId(
                1L,
                "6454",
                "BATERIA 100 AH MAXLIFE COM MANUTENÇÃO",
                null,
                "1.16.",
                "BATERIAS"
        );

        when(productRepository.findAllByCategory_CodeAndName(
                "1.16.",
                "BATERIA 100 AH MAXLIFE COMMANUTENÇÃO"
        )).thenReturn(List.of());
        when(productRepository.findAllByCategory_Code("1.16."))
                .thenReturn(List.of(product));

        Product resolved = productLookupService.findProductByCategoryCodeAndName(
                "1.16.",
                "BATERIA 100 AH MAXLIFE COMMANUTENÇÃO"
        ).orElseThrow();

        assertSame(product, resolved);
    }

    @Test
    void throwsWhenPurchasingMatchResolvesToMultipleProducts() {
        Product first = productWithId(1L, "2552", "GASOLINA ORIGINAL C", "123");
        Product second = productWithId(2L, "2553", "GASOLINA ORIGINAL C", "456");

        when(productRepository.findAllByCategory_CodeAndName("1.1.", "GASOLINA ORIGINAL C"))
                .thenReturn(List.of(first, second));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productLookupService.findProductByCategoryCodeAndName(
                        "1.1.",
                        "GASOLINA ORIGINAL C"
                )
        );

        assertEquals(
                "Conflicting products found for purchasing row with category code '1.1.' and name 'GASOLINA ORIGINAL C'",
                exception.getMessage()
        );
    }

    @Test
    void throwsWhenNormalizedPurchasingMatchResolvesToMultipleProducts() {
        Product first = productWithId(
                1L,
                "6454",
                "BATERIA 100 AH MAXLIFE COM MANUTENÇÃO",
                null,
                "1.16.",
                "BATERIAS"
        );
        Product second = productWithId(
                2L,
                "6455",
                "BATERIA 100 AH MAXLIFE COM MANUTENCAO",
                null,
                "1.16.",
                "BATERIAS"
        );

        when(productRepository.findAllByCategory_CodeAndName(
                "1.16.",
                "BATERIA 100 AH MAXLIFE COMMANUTENÇÃO"
        )).thenReturn(List.of());
        when(productRepository.findAllByCategory_Code("1.16."))
                .thenReturn(List.of(first, second));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productLookupService.findProductByCategoryCodeAndName(
                        "1.16.",
                        "BATERIA 100 AH MAXLIFE COMMANUTENÇÃO"
                )
        );

        assertEquals(
                "Conflicting products found for purchasing row with category code '1.16.' and name 'BATERIA 100 AH MAXLIFE COMMANUTENÇÃO'",
                exception.getMessage()
        );
    }

    private Product productWithId(Long id, String code, String name, String barcode) {
        return productWithId(id, code, name, barcode, "1.1.", "COMBUSTIVEIS");
    }

    private Product productWithId(
            Long id,
            String code,
            String name,
            String barcode,
            String categoryCode,
            String categoryName
    ) {
        Product product = new Product(
                code,
                name,
                new BigDecimal("6.49"),
                new BigDecimal("5.75"),
                new BigDecimal("11.44"),
                new Category(categoryCode, categoryName),
                barcode
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
