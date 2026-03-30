package com.postoBackend.backend.dataImport.processor.purchasing;

import com.postoBackend.backend.dataImport.context.ImportContext;
import com.postoBackend.backend.dataImport.reader.CsvReader;
import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.service.ProductLookupService;
import com.postoBackend.backend.service.ProductUpsertService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchasingProcessorTest {

    @Mock
    private CsvReader csvReader;

    @Mock
    private ProductLookupService productLookupService;

    @Mock
    private ProductUpsertService productUpsertService;

    private final PurchasingMapper purchasingMapper = new PurchasingMapper();

    @Test
    void updatesOnlyTheLatestPurchaseCostPerProduct() {
        PurchasingProcessor purchasingProcessor = new PurchasingProcessor(
                csvReader,
                purchasingMapper,
                productLookupService,
                productUpsertService
        );

        ImportContext context = new ImportContext(
                "purchasing",
                Path.of("data/purchasing/purchasing.csv"),
                "purchasing.csv"
        );
        Product product = new Product(
                "2171",
                "DIESEL S10 ORIGINAL",
                new BigDecimal("7.69"),
                null,
                null,
                new Category("1.1.", "COMBUSTIVEIS"),
                null
        );

        when(csvReader.read(context.getFilePath())).thenReturn(List.of(
                new String[] { "Grupo: 1.1. - COMBUSTIVEIS", "", "" },
                new String[] { "DIESEL S10 ORIGINAL", "", "", "", "28/02/2021", "SALDOINICIAL", "3.082", "", "", "3,2775", "", "", "10.101,26" },
                new String[] { "DIESEL S10 ORIGINAL", "", "", "", "22/04/2021", "413864", "5.000", "", "", "3,7035", "", "", "18.517,50" },
                new String[] { "DIESEL S10 ORIGINAL", "", "10/03/2023", "476608", "5.000", "5,3507", "", "", "26.753,50" }
        ));
        when(productLookupService.findProductByCategoryCodeAndName("1.1.", "DIESEL S10 ORIGINAL"))
                .thenReturn(Optional.of(product));

        purchasingProcessor.process(context);

        verify(productLookupService).findProductByCategoryCodeAndName("1.1.", "DIESEL S10 ORIGINAL");
        verify(productUpsertService).updateCost(product, new BigDecimal("5.3507"));
    }

    @Test
    void skipsRowsWhenMatchingProductCannotBeResolved() {
        PurchasingProcessor purchasingProcessor = new PurchasingProcessor(
                csvReader,
                purchasingMapper,
                productLookupService,
                productUpsertService
        );

        ImportContext context = new ImportContext(
                "purchasing",
                Path.of("data/purchasing/purchasing.csv"),
                "purchasing.csv"
        );

        when(csvReader.read(context.getFilePath())).thenReturn(List.of(
                new String[] { "Grupo: 1.1. - COMBUSTIVEIS", "", "" },
                new String[] { "DIESEL S10 ORIGINAL", "", "10/03/2023", "476608", "5.000", "5,3507", "", "", "26.753,50" }
        ));
        when(productLookupService.findProductByCategoryCodeAndName("1.1.", "DIESEL S10 ORIGINAL"))
                .thenReturn(Optional.empty());

        purchasingProcessor.process(context);

        verify(productLookupService).findProductByCategoryCodeAndName("1.1.", "DIESEL S10 ORIGINAL");
        verify(productUpsertService, never()).updateCost(any(), any());
    }

    @Test
    void usesInitialBalanceCostWhenNoNewerPurchaseExists() {
        PurchasingProcessor purchasingProcessor = new PurchasingProcessor(
                csvReader,
                purchasingMapper,
                productLookupService,
                productUpsertService
        );

        ImportContext context = new ImportContext(
                "purchasing",
                Path.of("data/purchasing/purchasing.csv"),
                "purchasing.csv"
        );
        Product product = new Product(
                "5202",
                "ALICATE BICO CURVO GEDORE",
                new BigDecimal("50.00"),
                null,
                null,
                new Category("1.29.", "ALICATES"),
                null
        );

        when(csvReader.read(context.getFilePath())).thenReturn(List.of(
                new String[] { "Grupo: 1.29. - ALICATES", "", "" },
                new String[] { "ALICATE BICO CURVO GEDORE", "", "", "", "28/02/2021", "SALDOINICIAL", "2", "", "", "27,58", "", "", "55,16" }
        ));
        when(productLookupService.findProductByCategoryCodeAndName("1.29.", "ALICATE BICO CURVO GEDORE"))
                .thenReturn(Optional.of(product));

        purchasingProcessor.process(context);

        verify(productLookupService).findProductByCategoryCodeAndName("1.29.", "ALICATE BICO CURVO GEDORE");
        verify(productUpsertService).updateCost(product, new BigDecimal("27.58"));
    }
}
