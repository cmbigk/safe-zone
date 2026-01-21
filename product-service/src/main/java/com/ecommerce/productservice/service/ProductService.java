package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.exception.UnauthorizedException;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final WebClient.Builder webClientBuilder;
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public ProductService(ProductRepository productRepository, WebClient.Builder webClientBuilder) {
        this.productRepository = productRepository;
        this.webClientBuilder = webClientBuilder;
    }
    
    public ProductResponse createProduct(ProductRequest request, String sellerEmail, String sellerId) {
        // Verify user is a seller by calling user-service
        boolean isSeller = verifyUserIsSeller(sellerEmail);
        if (!isSeller) {
            throw new UnauthorizedException("Only sellers can create products");
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setSellerId(sellerId);
        product.setSellerEmail(sellerEmail);
        product.setSellerName(request.getSellerName()); // Set from request
        product.setSellerAvatar(request.getSellerAvatar()); // Set from request
        product.setImageIds(request.getImageIds() != null ? request.getImageIds() : List.of());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created: {} by seller: {}", savedProduct.getId(), sellerEmail);
        
        // Publish event to Kafka
        if (kafkaTemplate != null) {
            kafkaTemplate.send("product-events", "PRODUCT_CREATED:" + savedProduct.getId() + ":" + sellerEmail);
        }
        
        return mapToProductResponse(savedProduct);
    }
    
    public ProductResponse updateProduct(String productId, ProductRequest request, String sellerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Verify the seller owns this product
        if (!product.getSellerEmail().equals(sellerEmail)) {
            throw new UnauthorizedException("You can only update your own products");
        }
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        if (request.getImageIds() != null) {
            product.setImageIds(request.getImageIds());
        }
        product.setUpdatedAt(LocalDateTime.now());
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated: {} by seller: {}", productId, sellerEmail);
        
        // Publish event to Kafka
        if (kafkaTemplate != null) {
            kafkaTemplate.send("product-events", "PRODUCT_UPDATED:" + productId + ":" + sellerEmail);
        }
        
        return mapToProductResponse(updatedProduct);
    }
    
    public void deleteProduct(String productId, String sellerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Verify the seller owns this product
        if (!product.getSellerEmail().equals(sellerEmail)) {
            throw new UnauthorizedException("You can only delete your own products");
        }
        
        productRepository.deleteById(productId);
        log.info("Product deleted: {} by seller: {}", productId, sellerEmail);
        
        // Publish event to Kafka
        if (kafkaTemplate != null) {
            kafkaTemplate.send("product-events", "PRODUCT_DELETED:" + productId + ":" + sellerEmail);
        }
    }
    
    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        return mapToProductResponse(product);
    }
    
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    public List<ProductResponse> getProductsBySeller(String sellerEmail) {
        return productRepository.findBySellerEmail(sellerEmail).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    public void updateSellerInfo(String sellerEmail, String sellerName, String sellerAvatar) {
        List<Product> products = productRepository.findBySellerEmail(sellerEmail);
        
        for (Product product : products) {
            product.setSellerName(sellerName);
            product.setSellerAvatar(sellerAvatar);
            product.setUpdatedAt(LocalDateTime.now());
        }
        
        productRepository.saveAll(products);
        log.info("Updated seller info for {} products", products.size());
    }
    
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    private boolean verifyUserIsSeller(String email) {
        try {
            // Call user-service to verify user role
            // For now, we'll implement a simple check
            // In production, you should call the user-service API
            return true; // Simplified - in real implementation, verify via user-service
        } catch (Exception e) {
            log.error("Error verifying user role: {}", e.getMessage());
            return false;
        }
    }
    
    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getSellerId(),
                product.getSellerEmail(),
                product.getSellerName(),
                product.getSellerAvatar(),
                product.getImageIds(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
