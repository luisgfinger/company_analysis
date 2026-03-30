package com.postoBackend.backend.repository;

import com.postoBackend.backend.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCode(String code);

    Optional<Category> findByNameIgnoreCase(String name);
}
