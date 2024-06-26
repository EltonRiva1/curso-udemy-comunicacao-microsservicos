package br.com.cursoudemy.productapi.modules.supplier.model;

import br.com.cursoudemy.productapi.modules.supplier.dto.SupplierRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SUPPLIER")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "NAME", nullable = false)
    private String name;

    public static Supplier of(SupplierRequest supplierRequest) {
        var supplier = new Supplier();
        BeanUtils.copyProperties(supplierRequest, supplier);
        return supplier;
    }
}
