package com.postoBackend.backend.domain.inventory;

import com.postoBackend.backend.domain.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(
        name = "inventory",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventory_product",
                columnNames = {"product_id"}
        )
)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_product"))
    private Product product;

    @Column(name = "quantity_in_stock")
    private BigDecimal quantityInStock;

    public Inventory() {
    }

    public Inventory(
            Product product,
            BigDecimal quantityInStock
    ) {
        this.product = product;
        this.quantityInStock = quantityInStock;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(BigDecimal quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    public String getProductCode() {
        return product != null ? product.getCode() : null;
    }

    public String getProductName() {
        return product != null ? product.getName() : null;
    }

    public String getProductBarcode() {
        return product != null ? product.getBarcode() : null;
    }

    public String getProductCategoryName() {
        return product != null ? product.getCategoryName() : null;
    }
}
