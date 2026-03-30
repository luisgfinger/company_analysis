package com.postoBackend.backend.dataImport.processor.product;

import com.postoBackend.backend.dataImport.context.ImportContext;
import com.postoBackend.backend.dataImport.reader.CsvReader;
import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.service.CategoryUpsertService;
import com.postoBackend.backend.service.ProductUpsertService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductProcessorTest {

    @Mock
    private CsvReader csvReader;

    @Mock
    private ProductRowFilter filter;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryUpsertService categoryUpsertService;

    @Mock
    private ProductUpsertService productUpsertService;

    @Test
    void persistsCategoryBeforeSavingProduct() {
        ProductProcessor productProcessor = new ProductProcessor(
                csvReader,
                filter,
                categoryMapper,
                productMapper,
                categoryUpsertService,
                productUpsertService
        );

        String[] row = {
                "Grupo",
                "A Vista",
                "1.1.",
                "COMBUSTIVEIS",
                "2171",
                "",
                "DIESEL S10",
                "27101921",
                "7,69"
        };
        ImportContext context = new ImportContext(
                "products",
                Path.of("data/products/products.csv"),
                "products.csv"
        );
        Category category = new Category("1.1.", "COMBUSTIVEIS");
        Category savedCategory = new Category("1.1.", "COMBUSTIVEIS");
        Product product = new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                null,
                null,
                null,
                null
        );

        when(csvReader.read(context.getFilePath())).thenReturn(Collections.singletonList(row));
        when(filter.isRelevant(row)).thenReturn(true);
        when(categoryMapper.map(row)).thenReturn(category);
        when(categoryUpsertService.saveOrUpdate(category)).thenReturn(savedCategory);
        when(productMapper.map(row)).thenReturn(product);

        productProcessor.process(context);

        InOrder inOrder = inOrder(categoryUpsertService, productUpsertService);
        inOrder.verify(categoryUpsertService).saveOrUpdate(category);
        inOrder.verify(productUpsertService).saveOrUpdate(product);
        assertSame(savedCategory, product.getCategory());
    }
}
