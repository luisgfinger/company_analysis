package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.repository.ProductRepository;
import com.postoBackend.backend.service.dto.ProductResponse;
import com.postoBackend.backend.service.dto.ProductSearchCriteria;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("name"),
            Sort.Order.asc("id")
    );

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductResponse> findPaged(ProductSearchCriteria criteria, Pageable pageable) {
        return productRepository.findAll(buildSpecification(criteria), pageable)
                .map(ProductResponse::from);
    }

    public List<ProductResponse> findAll(ProductSearchCriteria criteria, Sort sort) {
        Sort effectiveSort = sort.isSorted() ? sort : DEFAULT_SORT;

        return productRepository.findAll(buildSpecification(criteria), effectiveSort)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found for id " + id
                ));
    }

    private Specification<Product> buildSpecification(ProductSearchCriteria criteria) {
        Specification<Product> specification = allProducts();

        if (StringUtils.hasText(criteria.q())) {
            specification = specification.and(matchesAnyText(criteria.q()));
        }

        if (StringUtils.hasText(criteria.category())) {
            specification = specification.and(categoryNameContains(criteria.category()));
        }

        if (StringUtils.hasText(criteria.code())) {
            specification = specification.and(containsIgnoreCase("code", criteria.code()));
        }

        if (StringUtils.hasText(criteria.barcode())) {
            specification = specification.and(containsIgnoreCase("barcode", criteria.barcode()));
        }

        return specification;
    }

    private Specification<Product> allProducts() {
        return (root, query, cb) -> cb.conjunction();
    }

    private Specification<Product> matchesAnyText(String value) {
        return (root, query, cb) -> {
            String pattern = likePattern(value);

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.join("category", JoinType.LEFT).get("name")), pattern),
                    cb.like(cb.lower(root.get("barcode")), pattern)
            );
        };
    }

    private Specification<Product> categoryNameContains(String value) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.join("category", JoinType.LEFT).get("name")),
                likePattern(value)
        );
    }

    private Specification<Product> containsIgnoreCase(String field, String value) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get(field)),
                likePattern(value)
        );
    }

    private String likePattern(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
