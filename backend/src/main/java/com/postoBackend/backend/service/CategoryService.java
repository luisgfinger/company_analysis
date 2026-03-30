package com.postoBackend.backend.service;

import com.postoBackend.backend.repository.CategoryRepository;
import com.postoBackend.backend.service.dto.CategoryResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("name"),
            Sort.Order.asc("id")
    );

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll(DEFAULT_SORT)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
