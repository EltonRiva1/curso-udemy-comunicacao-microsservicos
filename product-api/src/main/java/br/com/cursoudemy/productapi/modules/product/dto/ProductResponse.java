package br.com.cursoudemy.productapi.modules.product.dto;

import br.com.cursoudemy.productapi.modules.category.dto.CategoryResponse;
import br.com.cursoudemy.productapi.modules.product.model.Product;
import br.com.cursoudemy.productapi.modules.supplier.dto.SupplierResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Integer id;
    private String name;
    @JsonProperty("quantity_available")
    private Integer quantityAvailable;
    private SupplierResponse supplierResponse;
    private CategoryResponse categoryResponse;
    @JsonProperty("created_at")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    public static ProductResponse of(Product product) {
        return ProductResponse.builder().id(product.getId()).name(product.getName()).quantityAvailable(product.getQuantityAvailable()).createdAt(product.getCreatedAt()).supplierResponse(SupplierResponse.of(product.getSupplier())).categoryResponse(CategoryResponse.of(product.getCategory())).build();
    }
}
