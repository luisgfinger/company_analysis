package com.postoBackend.backend.dataImport.processor.inventory;

import com.postoBackend.backend.dataImport.context.ImportContext;
import com.postoBackend.backend.dataImport.reader.CsvReader;
import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.inventory.Inventory;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.service.InventoryUpsertService;
import com.postoBackend.backend.service.ProductLookupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryProcessorTest {

    @Mock
    private CsvReader csvReader;

    @Mock
    private ProductLookupService productLookupService;

    @Mock
    private InventoryUpsertService inventoryUpsertService;

    @Test
    void persistsInventoryAcrossHeaderLayouts() {
        InventoryProcessor inventoryProcessor = createProcessor();

        String[] expandedHeader = {
                "Cod",
                "",
                "Produto",
                "",
                "",
                "Codigo barra",
                "Pr.Medio",
                "Pr.Venda",
                "",
                "",
                "",
                "Saldo Qtd",
                "",
                "",
                "Contagem",
                "",
                "",
                ""
        };
        String[] categoryRow = {"1.10. - AROMATIZANTES"};
        String[] expandedRow = {
                "8328 ",
                "",
                "AROMATIZANTE AREA 51",
                "",
                "",
                "",
                "13,145 ",
                "17,90 ",
                "",
                "",
                "",
                "17 ",
                "",
                "",
                "",
                "",
                "",
                ""
        };
        String[] compactSpacerHeader = {
                "Cod",
                "Produto",
                "",
                "Codigo barra",
                "Pr.Medio",
                "Pr.Venda",
                "",
                "Saldo Qtd",
                "",
                "",
                "Contagem",
                ""
        };
        String[] compactSpacerRow = {
                "8008 ",
                "BACONZITOS 34 GR.",
                "",
                "7892840823450",
                "2,796 ",
                "3,99 ",
                "",
                "3 ",
                "",
                "",
                "",
                ""
        };
        String[] compactHeader = {
                "Cod",
                "Produto",
                "Codigo barra",
                "Pr.Medio",
                "Pr.Venda",
                "",
                "Saldo Qtd",
                "",
                "",
                "Contagem",
                ""
        };
        String[] compactRow = {
                "6986 ",
                "TAMPA CLICK D 1004",
                "7893272000570",
                "17,78 ",
                "34,50 ",
                "",
                "2 ",
                "",
                "",
                "",
                ""
        };
        ImportContext context = inventoryContext();
        Product aromaticProduct = productWithId(1L, "8328", "AROMATIZANTE AREA 51", "1.10.", "AROMATIZANTES", null);
        Product snackProduct = productWithId(2L, "8008", "BACONZITOS 34 GR.", "1.13.", "ELMA CHIPS", "7892840823450");
        Product lidProduct = productWithId(3L, "6986", "TAMPA CLICK D 1004", "1.18.", "TAMPAS CLICK/TRINK", "7893272000570");

        when(csvReader.read(context.getFilePath())).thenReturn(List.of(
                new String[]{"Relacao para Contagem de Estoque"},
                expandedHeader,
                categoryRow,
                expandedRow,
                new String[]{"Total grupo 1.10. - AROMATIZANTES:"},
                compactSpacerHeader,
                compactSpacerRow,
                compactHeader,
                compactRow
        ));
        when(productLookupService.findProduct("8328", null)).thenReturn(Optional.of(aromaticProduct));
        when(productLookupService.findProduct("8008", "7892840823450")).thenReturn(Optional.of(snackProduct));
        when(productLookupService.findProduct("6986", "7893272000570")).thenReturn(Optional.of(lidProduct));
        when(inventoryUpsertService.saveOrUpdate(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryProcessor.process(context);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryUpsertService, times(3)).saveOrUpdate(inventoryCaptor.capture());

        List<Inventory> savedInventory = inventoryCaptor.getAllValues();
        assertSame(aromaticProduct, savedInventory.get(0).getProduct());
        assertEquals(new BigDecimal("17"), savedInventory.get(0).getQuantityInStock());
        assertSame(snackProduct, savedInventory.get(1).getProduct());
        assertEquals(new BigDecimal("3"), savedInventory.get(1).getQuantityInStock());
        assertSame(lidProduct, savedInventory.get(2).getProduct());
        assertEquals(new BigDecimal("2"), savedInventory.get(2).getQuantityInStock());
    }

    @Test
    void skipsRowsWhenProductCannotBeResolvedAndContinuesProcessing() {
        InventoryProcessor inventoryProcessor = createProcessor();

        String[] compactSpacerHeader = {
                "Cod",
                "Produto",
                "",
                "Codigo barra",
                "Pr.Medio",
                "Pr.Venda",
                "",
                "Saldo Qtd",
                "",
                "",
                "Contagem",
                ""
        };
        String[] unresolvedRow = {
                "6469 ",
                "BRUTUS AP 15 W 40 GRANEL",
                "",
                "",
                "10,000",
                "15,00",
                "",
                "10,000",
                "",
                "",
                "",
                ""
        };
        String[] resolvedRow = {
                "8008 ",
                "BACONZITOS 34 GR.",
                "",
                "7892840823450",
                "2,796 ",
                "3,99 ",
                "",
                "3 ",
                "",
                "",
                "",
                ""
        };
        ImportContext context = inventoryContext();
        Product product = productWithId(1L, "8008", "BACONZITOS 34 GR.", "1.13.", "ELMA CHIPS", "7892840823450");

        when(csvReader.read(context.getFilePath())).thenReturn(List.of(compactSpacerHeader, unresolvedRow, resolvedRow));
        when(productLookupService.findProduct("6469", null)).thenReturn(Optional.empty());
        when(productLookupService.findProduct("8008", "7892840823450")).thenReturn(Optional.of(product));
        when(inventoryUpsertService.saveOrUpdate(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryProcessor.process(context);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryUpsertService).saveOrUpdate(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertSame(product, savedInventory.getProduct());
        assertEquals("8008", savedInventory.getProductCode());
        assertEquals(new BigDecimal("3"), savedInventory.getQuantityInStock());
    }

    @Test
    void skipsRowsWhenLookupDetectsConflictingProducts() {
        InventoryProcessor inventoryProcessor = createProcessor();

        String[] compactHeader = {
                "Cod",
                "Produto",
                "Codigo barra",
                "Pr.Medio",
                "Pr.Venda",
                "",
                "Saldo Qtd",
                "",
                "",
                "Contagem",
                ""
        };
        String[] conflictingRow = {
                "6986 ",
                "TAMPA CLICK D 1004",
                "7893272000570",
                "17,78 ",
                "34,50 ",
                "",
                "2 ",
                "",
                "",
                "",
                ""
        };
        ImportContext context = inventoryContext();

        when(csvReader.read(context.getFilePath())).thenReturn(List.of(compactHeader, conflictingRow));
        when(productLookupService.findProduct("6986", "7893272000570"))
                .thenThrow(new IllegalStateException("Conflicting products found for inventory row"));

        inventoryProcessor.process(context);

        verify(inventoryUpsertService, never()).saveOrUpdate(any(Inventory.class));
    }

    private InventoryProcessor createProcessor() {
        return new InventoryProcessor(
                csvReader,
                new InventoryMapper(),
                productLookupService,
                inventoryUpsertService
        );
    }

    private ImportContext inventoryContext() {
        return new ImportContext(
                "inventory",
                Path.of("data/inventory/inventory.csv"),
                "inventory.csv"
        );
    }

    private Product productWithId(
            Long id,
            String code,
            String name,
            String categoryCode,
            String categoryName,
            String barcode
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
