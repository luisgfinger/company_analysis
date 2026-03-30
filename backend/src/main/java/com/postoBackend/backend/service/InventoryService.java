package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.inventory.Inventory;
import com.postoBackend.backend.repository.InventoryRepository;
import com.postoBackend.backend.service.dto.InventoryResponse;
import com.postoBackend.backend.service.dto.InventorySearchCriteria;
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
public class InventoryService {

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("product.name"),
            Sort.Order.asc("id")
    );

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Page<InventoryResponse> findPaged(InventorySearchCriteria criteria, Pageable pageable) {
        return inventoryRepository.findAll(buildSpecification(criteria), pageable)
                .map(InventoryResponse::from);
    }

    public List<InventoryResponse> findAll(InventorySearchCriteria criteria, Sort sort) {
        Sort effectiveSort = sort.isSorted() ? sort : DEFAULT_SORT;

        return inventoryRepository.findAll(buildSpecification(criteria), effectiveSort)
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }

    public InventoryResponse findById(Long id) {
        return inventoryRepository.findById(id)
                .map(InventoryResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found for id " + id
                ));
    }

    private Specification<Inventory> buildSpecification(InventorySearchCriteria criteria) {
        Specification<Inventory> specification = allInventory();

        if (StringUtils.hasText(criteria.q())) {
            specification = specification.and(matchesAnyText(criteria.q()));
        }

        if (StringUtils.hasText(criteria.code())) {
            specification = specification.and(productFieldContains("code", criteria.code()));
        }

        if (StringUtils.hasText(criteria.barcode())) {
            specification = specification.and(productFieldContains("barcode", criteria.barcode()));
        }

        if (StringUtils.hasText(criteria.category())) {
            specification = specification.and(categoryNameContains(criteria.category()));
        }

        return specification;
    }

    private Specification<Inventory> allInventory() {
        return (root, query, cb) -> cb.conjunction();
    }

    private Specification<Inventory> matchesAnyText(String value) {
        return (root, query, cb) -> {
            String pattern = likePattern(value);

            return cb.or(
                    cb.like(cb.lower(root.join("product", JoinType.INNER).get("name")), pattern),
                    cb.like(cb.lower(root.join("product", JoinType.INNER).get("code")), pattern),
                    cb.like(cb.lower(root.join("product", JoinType.INNER).get("barcode")), pattern),
                    cb.like(
                            cb.lower(root.join("product", JoinType.INNER).join("category", JoinType.LEFT).get("name")),
                            pattern
                    )
            );
        };
    }

    private Specification<Inventory> productFieldContains(String field, String value) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.join("product", JoinType.INNER).get(field)),
                likePattern(value)
        );
    }

    private Specification<Inventory> categoryNameContains(String value) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.join("product", JoinType.INNER).join("category", JoinType.LEFT).get("name")),
                likePattern(value)
        );
    }

    private String likePattern(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
