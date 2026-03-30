package com.postoBackend.backend.dataImport.service;

import com.postoBackend.backend.dataImport.context.ImportContext;
import com.postoBackend.backend.dataImport.context.ImportContextResolver;
import com.postoBackend.backend.dataImport.discovery.FileDiscoveryService;
import com.postoBackend.backend.dataImport.processor.ImportProcessor;
import com.postoBackend.backend.dataImport.processor.ImportProcessorResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private FileDiscoveryService fileDiscoveryService;

    @Mock
    private ImportContextResolver contextResolver;

    @Mock
    private ImportProcessorResolver processorResolver;

    @Mock
    private ImportProcessor productProcessor;

    @Mock
    private ImportProcessor purchasingProcessor;

    @Mock
    private ImportProcessor inventoryProcessor;

    @Test
    void executesProcessorsInDeclaredOrder() {
        ImportService importService = new ImportService(fileDiscoveryService, contextResolver, processorResolver);

        Path basePath = Path.of("data");
        Path inventoryFile = Path.of("data/inventory/inventory.csv");
        Path purchasingFile = Path.of("data/purchasing/purchasing.csv");
        Path productFile = Path.of("data/products/products.csv");
        ImportContext inventoryContext = new ImportContext("inventory", inventoryFile, "inventory.csv");
        ImportContext purchasingContext = new ImportContext("purchasing", purchasingFile, "purchasing.csv");
        ImportContext productContext = new ImportContext("products", productFile, "products.csv");

        when(fileDiscoveryService.findCsvFiles(basePath)).thenReturn(List.of(inventoryFile, productFile, purchasingFile));
        when(contextResolver.resolve(inventoryFile)).thenReturn(inventoryContext);
        when(contextResolver.resolve(purchasingFile)).thenReturn(purchasingContext);
        when(contextResolver.resolve(productFile)).thenReturn(productContext);
        when(processorResolver.resolve(inventoryContext)).thenReturn(inventoryProcessor);
        when(processorResolver.resolve(purchasingContext)).thenReturn(purchasingProcessor);
        when(processorResolver.resolve(productContext)).thenReturn(productProcessor);
        when(inventoryProcessor.getOrder()).thenReturn(20);
        when(purchasingProcessor.getOrder()).thenReturn(15);
        when(productProcessor.getOrder()).thenReturn(10);

        importService.executeImport(basePath);

        InOrder inOrder = inOrder(productProcessor, purchasingProcessor, inventoryProcessor);
        inOrder.verify(productProcessor).process(productContext);
        inOrder.verify(purchasingProcessor).process(purchasingContext);
        inOrder.verify(inventoryProcessor).process(inventoryContext);
    }
}
