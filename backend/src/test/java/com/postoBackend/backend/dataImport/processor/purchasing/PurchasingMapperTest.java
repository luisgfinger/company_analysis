package com.postoBackend.backend.dataImport.processor.purchasing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchasingMapperTest {

    private final PurchasingMapper purchasingMapper = new PurchasingMapper();

    @Test
    void detectsGroupContextFromHeaderRow() {
        PurchasingMapper.PurchasingGroup group = purchasingMapper.detectGroup(
                new String[] { "Grupo: 1.1. - COMBUSTIVEIS", "", "" }
        );

        assertNotNull(group);
        assertEquals("1.1.", group.categoryCode());
        assertEquals("COMBUSTIVEIS", group.categoryName());
    }

    @Test
    void recognizesPurchaseRowsAcrossDifferentCsvLayouts() {
        assertTrue(purchasingMapper.isPurchaseRow(new String[] {
                "DIESEL S10 ORIGINAL", "", "", "", "22/04/2021", "413864", "5.000", "", "", "3,7035", "", "", "18.517,50"
        }));

        assertTrue(purchasingMapper.isPurchaseRow(new String[] {
                "DIESEL S10 ORIGINAL", "", "10/03/2023", "476608", "5.000", "5,3507", "", "", "26.753,50"
        }));

        assertTrue(purchasingMapper.isPurchaseRow(new String[] {
                "ALICATE BICO CURVO GEDORE", "", "", "", "28/02/2021", "SALDOINICIAL", "2", "", "", "27,58", "", "", "55,16"
        }));

        assertFalse(purchasingMapper.isPurchaseRow(new String[] {
                "Fornecedor: AUTO POSTO GRANDO LTDA (cod. 1)", "", ""
        }));
    }

    @Test
    void mapsPurchaseRowUsingCompactedValues() {
        PurchasingImportRow row = purchasingMapper.map(
                new String[] {
                        "DIESEL S10 ORIGINAL", "", "10/03/2023", "476608", "5.000", "5,3507", "", "", "26.753,50"
                },
                new PurchasingMapper.PurchasingGroup("1.1.", "COMBUSTIVEIS")
        );

        assertEquals("1.1.", row.categoryCode());
        assertEquals("COMBUSTIVEIS", row.categoryName());
        assertEquals("DIESEL S10 ORIGINAL", row.productName());
        assertEquals(LocalDate.of(2023, 3, 10), row.movementDate());
        assertEquals("476608", row.document());
        assertEquals(new BigDecimal("5.3507"), row.cost());
    }

    @Test
    void mapsInitialBalanceRowsWhenTheyCarryCost() {
        PurchasingImportRow row = purchasingMapper.map(
                new String[] {
                        "ALICATE BICO CURVO GEDORE", "", "", "", "28/02/2021", "SALDOINICIAL", "2", "", "", "27,58", "", "", "55,16"
                },
                new PurchasingMapper.PurchasingGroup("1.29.", "ALICATES")
        );

        assertEquals("1.29.", row.categoryCode());
        assertEquals("ALICATES", row.categoryName());
        assertEquals("ALICATE BICO CURVO GEDORE", row.productName());
        assertEquals(LocalDate.of(2021, 2, 28), row.movementDate());
        assertEquals("SALDOINICIAL", row.document());
        assertEquals(new BigDecimal("27.58"), row.cost());
    }
}
