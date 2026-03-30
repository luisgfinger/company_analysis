package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@Transactional
public class ProductUpsertService {

    private final ProductRepository productRepository;

    public ProductUpsertService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product saveOrUpdate(Product incomingProduct) {
        Product productByCode = productRepository.findByCode(incomingProduct.getCode())
                .orElse(null);

        Product productByBarcode = hasBarcode(incomingProduct)
                ? productRepository.findByBarcode(incomingProduct.getBarcode()).orElse(null)
                : null;

        if (isConflictingMatch(productByCode, productByBarcode)) {
            throw new IllegalStateException(
                    "Conflicting products found for code '" + incomingProduct.getCode()
                            + "' and barcode '" + incomingProduct.getBarcode() + "'"
            );
        }

        Product target = productByCode != null ? productByCode : productByBarcode;

        if (target == null) {
            incomingProduct.setCost(null);
            return productRepository.save(incomingProduct);
        }

        target.setCode(incomingProduct.getCode());
        target.setName(incomingProduct.getName());
        target.setPrice(incomingProduct.getPrice());

        if (incomingProduct.getCategory() != null) {
            target.setCategory(incomingProduct.getCategory());
        }

        if (incomingProduct.getBarcode() != null) {
            target.setBarcode(incomingProduct.getBarcode());
        }

        return productRepository.save(target);
    }

    public Product updateCost(Product product, BigDecimal cost) {
        product.setCost(cost);
        return productRepository.save(product);
    }

    private boolean hasBarcode(Product product) {
        return StringUtils.hasText(product.getBarcode());
    }

    private boolean isConflictingMatch(Product productByCode, Product productByBarcode) {
        if (productByCode == null || productByBarcode == null) {
            return false;
        }

        return !productByCode.getId().equals(productByBarcode.getId());
    }
}
