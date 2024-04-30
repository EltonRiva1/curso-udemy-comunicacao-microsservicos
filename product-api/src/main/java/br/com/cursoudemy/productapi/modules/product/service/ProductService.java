package br.com.cursoudemy.productapi.modules.product.service;

import br.com.cursoudemy.productapi.config.exception.SuccessResponse;
import br.com.cursoudemy.productapi.config.exception.ValidationException;
import br.com.cursoudemy.productapi.modules.category.service.CategoryService;
import br.com.cursoudemy.productapi.modules.product.dto.ProductRequest;
import br.com.cursoudemy.productapi.modules.product.dto.ProductResponse;
import br.com.cursoudemy.productapi.modules.product.model.Product;
import br.com.cursoudemy.productapi.modules.product.repository.ProductRepository;
import br.com.cursoudemy.productapi.modules.supplier.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class ProductService {
    private static final Integer ZERO = 0;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private CategoryService categoryService;

    public ProductResponse save(ProductRequest productRequest) {
        this.validateProductDataInformed(productRequest);
        this.validateCategoryAndSupplierIdInformed(productRequest);
        var category = this.categoryService.findById(productRequest.getCategoryId());
        var supplier = this.supplierService.findById(productRequest.getSupplierId());
        var product = this.productRepository.save(Product.of(productRequest, supplier, category));
        return ProductResponse.of(product);
    }

    public ProductResponse update(ProductRequest productRequest, Integer id) {
        this.validateProductDataInformed(productRequest);
        this.validateInformedId(id);
        this.validateCategoryAndSupplierIdInformed(productRequest);
        var category = this.categoryService.findById(productRequest.getCategoryId());
        var supplier = this.supplierService.findById(productRequest.getSupplierId());
        var product = Product.of(productRequest, supplier, category);
        product.setId(id);
        this.productRepository.save(product);
        return ProductResponse.of(product);
    }

    private void validateProductDataInformed(ProductRequest productRequest) {
        if (isEmpty(productRequest.getName())) {
            throw new ValidationException("The product's name was not informed.");
        }
        if (isEmpty(productRequest.getQuantityAvailable())) {
            throw new ValidationException("The product's quantity was not informed.");
        }
        if (productRequest.getQuantityAvailable() <= ZERO) {
            throw new ValidationException("The quantity should be not less or equal zero.");
        }
    }

    private void validateCategoryAndSupplierIdInformed(ProductRequest productRequest) {
        if (isEmpty(productRequest.getCategoryId())) {
            throw new ValidationException("The category ID was not informed.");
        }
        if (isEmpty(productRequest.getSupplierId())) {
            throw new ValidationException("The supplier ID was not informed.");
        }
    }

    public List<ProductResponse> findAll() {
        return this.productRepository.findAll().stream().map(ProductResponse::of).collect(Collectors.toList());
    }

    public List<ProductResponse> findByName(String name) {
        if (isEmpty(name)) {
            throw new ValidationException("The product name must be informed.");
        }
        return this.productRepository.findByNameIgnoreCaseContaining(name).stream().map(ProductResponse::of).collect(Collectors.toList());
    }

    public List<ProductResponse> findBySupplierId(Integer supplierId) {
        if (isEmpty(supplierId)) {
            throw new ValidationException("The product's supplier ID must be informed.");
        }
        return this.productRepository.findBySupplierId(supplierId).stream().map(ProductResponse::of).collect(Collectors.toList());
    }

    public List<ProductResponse> findByCategoryId(Integer categoryId) {
        if (isEmpty(categoryId)) {
            throw new ValidationException("The product's category ID must be informed.");
        }
        return this.productRepository.findByCategoryId(categoryId).stream().map(ProductResponse::of).collect(Collectors.toList());
    }

    public ProductResponse findByIdResponse(Integer id) {
        return ProductResponse.of(this.findById(id));
    }

    public Product findById(Integer id) {
        this.validateInformedId(id);
        return this.productRepository.findById(id).orElseThrow(() -> new ValidationException("There's no product for the given ID."));
    }

    public Boolean existsByCategoryId(Integer categoryId) {
        return this.productRepository.existsByCategoryId(categoryId);
    }

    public Boolean existsBySupplierId(Integer supplierId) {
        return this.productRepository.existsBySupplierId(supplierId);
    }

    public SuccessResponse delete(Integer id) {
        this.validateInformedId(id);
        this.productRepository.deleteById(id);
        return SuccessResponse.create("The product was deleted.");
    }

    private void validateInformedId(Integer id) {
        if (isEmpty(id)) {
            throw new ValidationException("The supplier ID must be informed.");
        }
    }
}
