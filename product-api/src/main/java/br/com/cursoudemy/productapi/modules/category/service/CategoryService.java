package br.com.cursoudemy.productapi.modules.category.service;

import br.com.cursoudemy.productapi.config.exception.SuccessResponse;
import br.com.cursoudemy.productapi.config.exception.ValidationException;
import br.com.cursoudemy.productapi.modules.category.dto.CategoryRequest;
import br.com.cursoudemy.productapi.modules.category.dto.CategoryResponse;
import br.com.cursoudemy.productapi.modules.category.model.Category;
import br.com.cursoudemy.productapi.modules.category.repository.CategoryRepository;
import br.com.cursoudemy.productapi.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

    public CategoryResponse findByIdResponse(Integer id) {
        return CategoryResponse.of(this.findById(id));
    }

    public List<CategoryResponse> findAll() {
        return this.categoryRepository.findAll().stream().map(CategoryResponse::of).collect(Collectors.toList());
    }

    public List<CategoryResponse> findByDescription(String description) {
        if (isEmpty(description)) {
            throw new ValidationException("The category description must be informed.");
        }
        return this.categoryRepository.findByDescriptionIgnoreCaseContaining(description).stream().map(CategoryResponse::of).collect(Collectors.toList());
    }

    public Category findById(Integer id) {
        this.validateInformedId(id);
        return this.categoryRepository.findById(id).orElseThrow(() -> new ValidationException("There's no supplier for the given ID."));
    }

    public CategoryResponse save(CategoryRequest categoryRequest) {
        this.validateCategoryNameInformed(categoryRequest);
        var category = this.categoryRepository.save(Category.of(categoryRequest));
        return CategoryResponse.of(category);
    }

    public CategoryResponse update(CategoryRequest categoryRequest, Integer id) {
        this.validateCategoryNameInformed(categoryRequest);
        this.validateInformedId(id);
        var category = Category.of(categoryRequest);
        category.setId(id);
        this.categoryRepository.save(category);
        return CategoryResponse.of(category);
    }

    private void validateCategoryNameInformed(CategoryRequest categoryRequest) {
        if (isEmpty(categoryRequest.getDescription())) {
            throw new ValidationException("The category description was not informed.");
        }
    }

    public SuccessResponse delete(Integer id) {
        this.validateInformedId(id);
        if (this.productService.existsByCategoryId(id)) {
            throw new ValidationException("You cannot delete this category because it's already defined by a product.");
        }
        this.categoryRepository.deleteById(id);
        return SuccessResponse.create("The category was deleted.");
    }

    private void validateInformedId(Integer id) {
        if (isEmpty(id)) {
            throw new ValidationException("The category ID must be informed.");
        }
    }
}
