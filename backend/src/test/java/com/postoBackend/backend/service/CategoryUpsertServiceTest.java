package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryUpsertServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryUpsertService categoryUpsertService;

    @Test
    void savesNewCategoryWhenNoExistingCategoryMatches() {
        Category incoming = new Category("1.1.", "COMBUSTIVEIS");

        when(categoryRepository.findByCode("1.1.")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCase("COMBUSTIVEIS")).thenReturn(Optional.empty());
        when(categoryRepository.save(incoming)).thenReturn(incoming);

        Category saved = categoryUpsertService.saveOrUpdate(incoming);

        assertSame(incoming, saved);
        verify(categoryRepository).save(incoming);
    }

    @Test
    void updatesExistingCategoryWhenCodeAlreadyExists() {
        Category existing = new Category("1.1.", "COMBUSTIVEIS");
        Category incoming = new Category("1.1.", "COMBUSTIVEIS ADITIVADOS");

        when(categoryRepository.findByCode("1.1.")).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        Category saved = categoryUpsertService.saveOrUpdate(incoming);

        assertSame(existing, saved);
        assertEquals("COMBUSTIVEIS ADITIVADOS", existing.getName());
        verify(categoryRepository, never()).findByNameIgnoreCase("COMBUSTIVEIS ADITIVADOS");
    }

    @Test
    void updatesExistingCategoryWhenNameAlreadyExists() {
        Category existing = new Category("OLD", "COMBUSTIVEIS");
        Category incoming = new Category("1.1.", "COMBUSTIVEIS");

        when(categoryRepository.findByCode("1.1.")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCase("COMBUSTIVEIS")).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        Category saved = categoryUpsertService.saveOrUpdate(incoming);

        assertSame(existing, saved);
        assertEquals("1.1.", existing.getCode());
        assertEquals("COMBUSTIVEIS", existing.getName());
    }

    @Test
    void ignoresCategoryWhenRequiredFieldsAreMissing() {
        Category incoming = new Category("   ", "COMBUSTIVEIS");

        Category saved = categoryUpsertService.saveOrUpdate(incoming);

        assertNull(saved);
        verifyNoInteractions(categoryRepository);
    }
}
