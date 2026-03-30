package com.postoBackend.backend.domain.product;

import com.postoBackend.backend.domain.category.Category;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_products_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_products_barcode", columnNames = "barcode")
        }
)
public class Product {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal profitMargin;

    @Column(name = "cost")
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_products_category"))
    private Category category;

    private String barcode;

    public Product() {
    }

    public Product(
            String code,
            String name,
            BigDecimal price,
            BigDecimal cost,
            BigDecimal profitMargin,
            Category category,
            String barcode
    ) {
        this.code = normalizeValue(code);
        this.name = normalizeValue(name);
        this.price = price;
        this.cost = cost;
        this.profitMargin = profitMargin;
        this.category = category;
        this.barcode = normalizeValue(barcode);
        syncProfitMargin();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public BigDecimal getProfitMargin() {
        return calculateProfitMargin(price, cost);
    }

    public void setProfitMargin(BigDecimal profitMargin) {
        syncProfitMargin();
    }

    public String getBarcode() {
        return barcode;
    }

    public void setCode(String code) {
        this.code = normalizeValue(code);
    }

    public void setName(String name) {
        this.name = normalizeValue(name);
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
        syncProfitMargin();
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        syncProfitMargin();
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setBarcode(String barcode) {
        this.barcode = normalizeValue(barcode);
    }

    @PrePersist
    @PreUpdate
    private void syncProfitMargin() {
        this.profitMargin = calculateProfitMargin(price, cost);
    }

    private BigDecimal calculateProfitMargin(BigDecimal price, BigDecimal cost) {
        if (price == null || cost == null || price.signum() == 0) {
            return null;
        }

        return price.subtract(cost)
                .multiply(ONE_HUNDRED)
                .divide(price, 2, RoundingMode.HALF_UP);
    }

    private String normalizeValue(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}
