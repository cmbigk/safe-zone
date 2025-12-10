package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {
    
    private final ProductService productService;
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String sellerEmail,
            @RequestHeader(value = "X-User-Id", required = false) String sellerId) {
        ProductResponse response = productService.createProduct(request, sellerEmail, sellerId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String sellerEmail) {
        ProductResponse response = productService.updateProduct(id, request, sellerEmail);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Email", required = false) String sellerEmail) {
        productService.deleteProduct(id, sellerEmail);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/seller/{email}")
    public ResponseEntity<List<ProductResponse>> getProductsBySeller(@PathVariable String email) {
        List<ProductResponse> products = productService.getProductsBySeller(email);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String category) {
        List<ProductResponse> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
    
    @PutMapping("/seller/{email}")
    public ResponseEntity<Void> updateSellerInfo(
            @PathVariable String email,
            @RequestParam String sellerName,
            @RequestParam(required = false) String sellerAvatar) {
        productService.updateSellerInfo(email, sellerName, sellerAvatar);
        return ResponseEntity.ok().build();
    }
}
