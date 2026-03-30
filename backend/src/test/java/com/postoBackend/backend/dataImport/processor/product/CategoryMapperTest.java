package com.postoBackend.backend.dataImport.processor.product;

import com.postoBackend.backend.domain.category.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CategoryMapperTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();

    @Test
    void mapsCategoryCodeAndNameFromProductRow() {
        String[] row = {
                "Grupo",
                "A Vista",
                " 1.1. ",
                " COMBUSTIVEIS ",
                "2171",
                "123",
                "DIESEL S10",
                "27101921",
                "7,69"
        };

        Category category = categoryMapper.map(row);

        assertEquals("1.1.", category.getCode());
        assertEquals("COMBUSTIVEIS", category.getName());
    }

    @Test
    void returnsNullWhenCategoryCodeAndNameAreMissing() {
        String[] row = {
                "Grupo",
                "A Vista",
                "   ",
                null,
                "2171",
                "123",
                "DIESEL S10",
                "27101921",
                "7,69"
        };

        Category category = categoryMapper.map(row);

        assertNull(category);
    }
}
