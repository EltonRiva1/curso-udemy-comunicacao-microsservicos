package br.com.cursoudemy.productapi.modules.product.model;

import br.com.cursoudemy.productapi.modules.category.model.Category;
import br.com.cursoudemy.productapi.modules.product.dto.ProductRequest;
import br.com.cursoudemy.productapi.modules.supplier.model.Supplier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRODUCT")
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "NAME", nullable = false)
    private String name;
    @ManyToOne
    @JoinColumn(name = "FK_SUPPLIER", nullable = false)
    private Supplier supplier;
    @ManyToOne
    @JoinColumn(name = "FK_CATEGORY", nullable = false)
    private Category category;
    @Column(name = "QUANTITY_AVAILABLE", nullable = false)
    private Integer quantityAvailable;
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static Product of(ProductRequest productRequest, Supplier supplier, Category category) {
        return Product.builder().name(productRequest.getName()).quantityAvailable(productRequest.getQuantityAvailable()).supplier(supplier).category(category).build();
    }

    public void updateStock(Integer quantity) {
        this.quantityAvailable = this.quantityAvailable - quantity;
    }
}
