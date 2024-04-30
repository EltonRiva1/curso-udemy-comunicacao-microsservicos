package br.com.cursoudemy.productapi.modules.product.controller;

import br.com.cursoudemy.productapi.config.exception.SuccessResponse;
import br.com.cursoudemy.productapi.modules.category.dto.CategoryRequest;
import br.com.cursoudemy.productapi.modules.category.dto.CategoryResponse;
import br.com.cursoudemy.productapi.modules.product.dto.ProductRequest;
import br.com.cursoudemy.productapi.modules.product.dto.ProductResponse;
import br.com.cursoudemy.productapi.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping
    public ProductResponse save(@RequestBody ProductRequest productRequest) {
        return this.productService.save(productRequest);
    }

    @GetMapping
    public List<ProductResponse> findAll() {
        return this.productService.findAll();
    }

    @GetMapping("{id}")
    public ProductResponse findById(@PathVariable Integer id) {
        return this.productService.findByIdResponse(id);
    }

    @GetMapping("name/{name}")
    public List<ProductResponse> findByName(@PathVariable String name) {
        return this.productService.findByName(name);
    }

    @GetMapping("category/{categoryId}")
    public List<ProductResponse> findByCategoryId(@PathVariable Integer categoryId) {
        return this.productService.findByCategoryId(categoryId);
    }

    @GetMapping("supplier/{supplierId}")
    public List<ProductResponse> findBySupplierId(@PathVariable Integer supplierId) {
        return this.productService.findBySupplierId(supplierId);
    }

    @DeleteMapping("{id}")
    public SuccessResponse delete(@PathVariable Integer id) {
        return this.productService.delete(id);
    }

    @PutMapping("{id}")
    public ProductResponse update(@RequestBody ProductRequest productRequest, @PathVariable Integer id) {
        return this.productService.update(productRequest, id);
    }
}