package br.com.cursoudemy.productapi.modules.product.service;

import br.com.cursoudemy.productapi.config.RequestUtil;
import br.com.cursoudemy.productapi.config.exception.SuccessResponse;
import br.com.cursoudemy.productapi.config.exception.ValidationException;
import br.com.cursoudemy.productapi.modules.category.service.CategoryService;
import br.com.cursoudemy.productapi.modules.product.dto.*;
import br.com.cursoudemy.productapi.modules.product.model.Product;
import br.com.cursoudemy.productapi.modules.product.repository.ProductRepository;
import br.com.cursoudemy.productapi.modules.sales.client.SalesClient;
import br.com.cursoudemy.productapi.modules.sales.dto.SalesConfirmationDTO;
import br.com.cursoudemy.productapi.modules.sales.dto.SalesProductResponse;
import br.com.cursoudemy.productapi.modules.sales.enums.SalesStatus;
import br.com.cursoudemy.productapi.modules.sales.rabbitmq.SalesConfirmationSender;
import br.com.cursoudemy.productapi.modules.supplier.service.SupplierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@Slf4j
@AllArgsConstructor
public class ProductService {
    private static final Integer ZERO = 0;
    private static final String TRANSACTION_ID = "transactionid", SERVICE_ID = "serviceid", AUTHORIZATION = "Authorization";
    private final ProductRepository productRepository;
    private final SupplierService supplierService;
    private final CategoryService categoryService;
    private final SalesConfirmationSender salesConfirmationSender;
    private final SalesClient salesClient;
    private final ObjectMapper objectMapper;

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
        if (!this.productRepository.existsById(id)) {
            throw new ValidationException("The product does not exists.");
        }
        var sales = this.getSalesByProductId(id);
        if (!isEmpty(sales.getSalesIds())) {
            throw new ValidationException("The product cannot be deleted. There are sales for it.");
        }
        this.productRepository.deleteById(id);
        return SuccessResponse.create("The product was deleted.");
    }

    private void validateInformedId(Integer id) {
        if (isEmpty(id)) {
            throw new ValidationException("The supplier ID must be informed.");
        }
    }

    public void updateProductStock(ProductStockDTO productStockDTO) {
        try {
            this.validateStockUpdateData(productStockDTO);
            this.updateStock(productStockDTO);
        } catch (Exception e) {
            log.error("Error while trying to update stock for message with error: {}", e.getMessage(), e);
            var rejectedMessage = new SalesConfirmationDTO(productStockDTO.getSalesId(), SalesStatus.REJECTED, productStockDTO.getTransactionid());
            this.salesConfirmationSender.sendSalesConfirmationMessage(rejectedMessage);
        }
    }

    @Transactional
    private void validateStockUpdateData(ProductStockDTO productStockDTO) {
        if (isEmpty(productStockDTO) || isEmpty(productStockDTO.getSalesId())) {
            throw new ValidationException("The product data and the sales ID must be informed.");
        }
        if (isEmpty(productStockDTO.getProducts())) {
            throw new ValidationException("The sales products must be informed.");
        }
        productStockDTO.getProducts().forEach(salesProduct -> {
            if (isEmpty(salesProduct.getQuantity()) || isEmpty(salesProduct.getProductId())) {
                throw new ValidationException("The productID and the quantity must be informed.");
            }
        });
    }

    @Transactional
    private void updateStock(ProductStockDTO productStockDTO) {
        var productsForUpdate = new ArrayList<Product>();
        productStockDTO.getProducts().forEach(salesProduct -> {
            var existingProduct = this.findById(salesProduct.getProductId());
            this.validateQuantityInStock(salesProduct, existingProduct);
            existingProduct.updateStock(salesProduct.getQuantity());
            productsForUpdate.add(existingProduct);
        });
        if (!isEmpty(productsForUpdate)) {
            this.productRepository.saveAll(productsForUpdate);
            var approvedMessage = new SalesConfirmationDTO(productStockDTO.getSalesId(), SalesStatus.APPROVED, productStockDTO.getTransactionid());
            this.salesConfirmationSender.sendSalesConfirmationMessage(approvedMessage);
        }
    }

    private void validateQuantityInStock(ProductQuantityDTO productQuantityDTO, Product product) {
        if (productQuantityDTO.getQuantity() > product.getQuantityAvailable()) {
            throw new ValidationException(String.format("The product %s is out of stock.", product.getId()));
        }
    }

    public ProductSalesResponse findProductSales(Integer id) {
        var product = this.findById(id);
        var sales = this.getSalesByProductId(id);
        return ProductSalesResponse.of(product, sales.getSalesIds());
    }

    private SalesProductResponse getSalesByProductId(Integer productId) {
        try {
            var currentRequest = RequestUtil.getCurrentRequest();
            var transactionId = currentRequest.getHeader(TRANSACTION_ID);
            var serviceId = currentRequest.getAttribute(SERVICE_ID);
            var token = currentRequest.getHeader(AUTHORIZATION);
            log.info("Sending GET request to orders by productId with data {} | [transactionId: {} | serviceId: {}]", productId, transactionId, serviceId);
            var response = this.salesClient.findSalesByProductId(productId, token, transactionId).orElseThrow(() -> new ValidationException("The sales was not found by this product."));
            log.info("Receiving response from orders by productId with data {} | [transactionId: {} | serviceId: {}]", this.objectMapper.writeValueAsString(response), transactionId, serviceId);
            return response;
        } catch (Exception e) {
            log.error("Error trying to call Sales-API: {}", e.getMessage());
            throw new ValidationException("The sales could not be found.");
        }
    }

    public SuccessResponse checkProductsStock(ProductCheckStockRequest productCheckStockRequest) {
        try {
            var currentRequest = RequestUtil.getCurrentRequest();
            var transactionId = currentRequest.getHeader(TRANSACTION_ID);
            var serviceId = currentRequest.getAttribute(SERVICE_ID);
            log.info("Request to POST product stock with data {} | [transactionId: {} | serviceId: {}]", this.objectMapper.writeValueAsString(productCheckStockRequest), transactionId, serviceId);
            if (isEmpty(productCheckStockRequest) || isEmpty(productCheckStockRequest.getProducts())) {
                throw new ValidationException("The request data must be informed.");
            }
            productCheckStockRequest.getProducts().forEach(this::validateStock);
            var response = SuccessResponse.create("The stock is ok!");
            log.info("Response to POST product stock with data {} | [transactionId: {} | serviceId: {}]", this.objectMapper.writeValueAsString(response), transactionId, serviceId);
            return response;
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void validateStock(ProductQuantityDTO productQuantityDTO) {
        if (isEmpty(productQuantityDTO.getProductId()) || isEmpty(productQuantityDTO.getQuantity())) {
            throw new ValidationException("Product ID and quantity must be informed.");
        }
        var product = this.findById(productQuantityDTO.getProductId());
        if (productQuantityDTO.getQuantity() > product.getQuantityAvailable()) {
            throw new ValidationException(String.format("The product %s is out of stock.", product.getId()));
        }
    }
}
