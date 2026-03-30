package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.product.Product;
import com.postoBackend.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductLookupService {

    private final ProductRepository productRepository;

    public ProductLookupService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<Product> findProduct(String code, String barcode) {
        Product productByCode = StringUtils.hasText(code)
                ? productRepository.findByCode(code).orElse(null)
                : null;

        Product productByBarcode = StringUtils.hasText(barcode)
                ? productRepository.findByBarcode(barcode).orElse(null)
                : null;

        if (isConflictingMatch(productByCode, productByBarcode)) {
            throw new IllegalStateException(
                    "Conflicting products found for inventory row with code '" + normalizeLabel(code)
                            + "' and barcode '" + normalizeLabel(barcode) + "'"
            );
        }

        Product product = productByCode != null ? productByCode : productByBarcode;

        return Optional.ofNullable(product);
    }

    public Product findRequiredProduct(String code, String barcode) {
        return findProduct(code, barcode)
                .orElseThrow(() -> new IllegalStateException(
                        "Product not found for inventory row with code '" + normalizeLabel(code)
                                + "' and barcode '" + normalizeLabel(barcode) + "'"
                ));
    }

    public Optional<Product> findProductByCategoryCodeAndName(String categoryCode, String name) {
        if (!StringUtils.hasText(categoryCode) || !StringUtils.hasText(name)) {
            return Optional.empty();
        }

        String normalizedCategoryCode = categoryCode.trim();
        String normalizedName = name.trim();
        List<Product> matches = productRepository.findAllByCategory_CodeAndName(
                normalizedCategoryCode,
                normalizedName
        );

        if (!matches.isEmpty()) {
            return Optional.of(resolveSinglePurchasingMatch(matches, normalizedCategoryCode, normalizedName));
        }

        List<Product> normalizedMatches = productRepository.findAllByCategory_Code(normalizedCategoryCode).stream()
                .filter(product -> hasEquivalentPurchasingName(product.getName(), normalizedName))
                .toList();

        if (normalizedMatches.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(resolveSinglePurchasingMatch(normalizedMatches, normalizedCategoryCode, normalizedName));
    }

    private boolean isConflictingMatch(Product productByCode, Product productByBarcode) {
        if (productByCode == null || productByBarcode == null) {
            return false;
        }

        return !productByCode.getId().equals(productByBarcode.getId());
    }

    private Product resolveSinglePurchasingMatch(List<Product> matches, String categoryCode, String name) {
        if (matches.size() > 1) {
            throw new IllegalStateException(
                    "Conflicting products found for purchasing row with category code '"
                            + normalizeLabel(categoryCode) + "' and name '" + normalizeLabel(name) + "'"
            );
        }

        return matches.get(0);
    }

    private boolean hasEquivalentPurchasingName(String left, String right) {
        return normalizePurchasingName(left).equals(normalizePurchasingName(right));
    }

    private String normalizePurchasingName(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeLabel(String value) {
        return StringUtils.hasText(value) ? value.trim() : "<empty>";
    }
}
