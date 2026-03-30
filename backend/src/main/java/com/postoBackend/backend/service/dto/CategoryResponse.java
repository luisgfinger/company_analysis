package com.postoBackend.backend.service.dto;

import com.postoBackend.backend.domain.category.Category;

public record CategoryResponse(
        Long id,
        String code,
        String name
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName()
        );
    }
}
