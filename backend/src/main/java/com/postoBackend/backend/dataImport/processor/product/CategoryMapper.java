package com.postoBackend.backend.dataImport.processor.product;

import com.postoBackend.backend.domain.category.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    private static final int CATEGORY_CODE_INDEX = 2;
    private static final int CATEGORY_NAME_INDEX = 3;

    public Category map(String[] row) {
        String code = readCell(row, CATEGORY_CODE_INDEX);
        String name = readCell(row, CATEGORY_NAME_INDEX);

        if (code == null && name == null) {
            return null;
        }

        return new Category(code, name);
    }

    private String readCell(String[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return null;
        }

        String trimmed = row[index].trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}
