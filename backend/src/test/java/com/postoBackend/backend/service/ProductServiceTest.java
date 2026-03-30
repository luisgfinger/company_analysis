package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.category.Category;
import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.repository.ProductRepository;
import com.postoBackend.backend.service.dto.ProductResponse;
import com.postoBackend.backend.service.dto.ProductSearchCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findPagedMapsRepositoryPage() {
        Product product = new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                category("1.1.", "COMBUSTIVEIS"),
                "123"
        );
        PageRequest pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<ProductResponse> response = productService.findPaged(
                new ProductSearchCriteria("diesel", null, null, null),
                pageable
        );

        assertEquals(1, response.getTotalElements());
        assertEquals("DIESEL S10", response.getContent().get(0).name());
        assertEquals("2171", response.getContent().get(0).code());
        assertEquals(new BigDecimal("6.04"), response.getContent().get(0).cost());
        assertEquals(new BigDecimal("21.46"), response.getContent().get(0).profitMargin());
    }

    @Test
    void findAllUsesDefaultSortWhenSortIsUnsorted() {
        Product product = new Product(
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                category("1.1.", "COMBUSTIVEIS"),
                "123"
        );
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        when(productRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(product));

        List<ProductResponse> response = productService.findAll(
                new ProductSearchCriteria(null, "comb", null, null),
                Sort.unsorted()
        );

        verify(productRepository).findAll(any(Specification.class), sortCaptor.capture());

        assertEquals("name: ASC,id: ASC", sortCaptor.getValue().toString());
        assertEquals(1, response.size());
        assertEquals("COMBUSTIVEIS", response.get(0).category());
        assertEquals(new BigDecimal("6.04"), response.get(0).cost());
        assertEquals(new BigDecimal("21.46"), response.get(0).profitMargin());
    }

    @Test
    void findByIdThrowsNotFoundWhenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.findById(999L)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Product not found for id 999", exception.getReason());
    }

    private Category category(String code, String name) {
        return new Category(code, name);
    }
}
