package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class CategoryUpsertService {

    private final CategoryRepository categoryRepository;

    public CategoryUpsertService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category saveOrUpdate(Category incomingCategory) {
        if (incomingCategory == null || !hasRequiredFields(incomingCategory)) {
            return null;
        }

        Category existingByCode = categoryRepository.findByCode(incomingCategory.getCode())
                .orElse(null);

        if (existingByCode != null) {
            existingByCode.setName(incomingCategory.getName());
            return categoryRepository.save(existingByCode);
        }

        Category existingByName = categoryRepository.findByNameIgnoreCase(incomingCategory.getName())
                .orElse(null);

        if (existingByName != null) {
            existingByName.setCode(incomingCategory.getCode());
            existingByName.setName(incomingCategory.getName());
            return categoryRepository.save(existingByName);
        }

        return categoryRepository.save(incomingCategory);
    }

    private boolean hasRequiredFields(Category category) {
        return StringUtils.hasText(category.getCode()) && StringUtils.hasText(category.getName());
    }
}
