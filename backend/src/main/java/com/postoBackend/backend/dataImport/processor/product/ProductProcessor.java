package com.postoBackend.backend.dataImport.processor.product;

import com.postoBackend.backend.dataImport.context.ImportContext;
import com.postoBackend.backend.dataImport.processor.ImportProcessor;
import com.postoBackend.backend.dataImport.reader.CsvReader;
import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.service.CategoryUpsertService;
import com.postoBackend.backend.service.ProductUpsertService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductProcessor implements ImportProcessor {

    private final CsvReader csvReader;
    private final ProductRowFilter filter;
    private final CategoryMapper categoryMapper;
    private final ProductMapper mapper;
    private final CategoryUpsertService categoryUpsertService;
    private final ProductUpsertService productUpsertService;

    public ProductProcessor(
            CsvReader csvReader,
            ProductRowFilter filter,
            CategoryMapper categoryMapper,
            ProductMapper mapper,
            CategoryUpsertService categoryUpsertService,
            ProductUpsertService productUpsertService
    ) {
        this.csvReader = csvReader;
        this.filter = filter;
        this.categoryMapper = categoryMapper;
        this.mapper = mapper;
        this.categoryUpsertService = categoryUpsertService;
        this.productUpsertService = productUpsertService;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void process(ImportContext context) {

        List<String[]> rows = csvReader.read(context.getFilePath());

        for (String[] row : rows) {

            if (!filter.isRelevant(row)) {
                continue;
            }

            Category category = categoryMapper.map(row);
            Category savedCategory = categoryUpsertService.saveOrUpdate(category);

            Product product = mapper.map(row);
            product.setCategory(savedCategory);

            productUpsertService.saveOrUpdate(product);
        }
    }
}
