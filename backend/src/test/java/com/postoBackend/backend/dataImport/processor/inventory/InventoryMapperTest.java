package com.postoBackend.backend.dataImport.processor.inventory;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InventoryMapperTest {

    private final InventoryMapper inventoryMapper = new InventoryMapper();

    @Test
    void mapsExpandedPageRow() {
        String[] header = {
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
        String[] row = {
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

        InventoryMapper.InventoryColumns columns = inventoryMapper.detectColumns(header);
        assertNotNull(columns);

        InventoryImportRow inventoryRow = inventoryMapper.map(row, columns);

        assertEquals("8328", inventoryRow.productCode());
        assertNull(inventoryRow.productBarcode());
        assertEquals(new BigDecimal("17"), inventoryRow.quantityInStock());
    }

    @Test
    void mapsCompactRowWithBarcodeSpacer() {
        String[] header = {
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
        String[] row = {
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

        InventoryMapper.InventoryColumns columns = inventoryMapper.detectColumns(header);
        assertNotNull(columns);

        InventoryImportRow inventoryRow = inventoryMapper.map(row, columns);

        assertEquals("8008", inventoryRow.productCode());
        assertEquals("7892840823450", inventoryRow.productBarcode());
        assertEquals(new BigDecimal("3"), inventoryRow.quantityInStock());
    }

    @Test
    void mapsCompactRowWithoutBarcodeSpacer() {
        String[] header = {
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
        String[] row = {
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

        InventoryMapper.InventoryColumns columns = inventoryMapper.detectColumns(header);
        assertNotNull(columns);

        InventoryImportRow inventoryRow = inventoryMapper.map(row, columns);

        assertEquals("6986", inventoryRow.productCode());
        assertEquals("7893272000570", inventoryRow.productBarcode());
        assertEquals(new BigDecimal("2"), inventoryRow.quantityInStock());
    }

    @Test
    void mapsBlankQuantityAsNull() {
        String[] header = {
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
        String[] row = {
                "2857 ",
                "BATATA LAYS CLASSICA 70 GR",
                "",
                "",
                "6,117 ",
                "9,99 ",
                "",
                " ",
                "",
                "",
                "",
                ""
        };

        InventoryMapper.InventoryColumns columns = inventoryMapper.detectColumns(header);
        assertNotNull(columns);

        InventoryImportRow inventoryRow = inventoryMapper.map(row, columns);

        assertEquals("2857", inventoryRow.productCode());
        assertNull(inventoryRow.productBarcode());
        assertNull(inventoryRow.quantityInStock());
    }
}
