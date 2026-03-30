package com.postoBackend.backend.repository;

import com.postoBackend.backend.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByCode(String code);

    Optional<Product> findByBarcode(String barcode);

    List<Product> findAllByCategory_Code(String categoryCode);

    List<Product> findAllByCategory_CodeAndName(String categoryCode, String name);
}
