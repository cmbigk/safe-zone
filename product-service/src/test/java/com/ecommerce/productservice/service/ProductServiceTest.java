package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.exception.UnauthorizedException;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 * Tests the core business logic of product management including CRUD operations and seller authorization
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId("product123");
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(100);
        testProduct.setCategory("Electronics");
        testProduct.setSellerId("seller123");
        testProduct.setSellerEmail("seller@example.com");
        testProduct.setSellerName("John Seller");

        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(new BigDecimal("99.99"));
        productRequest.setStock(100);
        productRequest.setCategory("Electronics");
        productRequest.setSellerName("John Seller");
        productRequest.setSellerAvatar("/avatars/seller.jpg");
    }

    @Test
    @DisplayName("Should successfully retrieve product by ID")
    void testGetProductById_Success() {
        // Arrange
        when(productRepository.findById("product123")).thenReturn(Optional.of(testProduct));

        // Act
        ProductResponse response = productService.getProductById("product123");

        // Assert
        assertNotNull(response);
        assertEquals("product123", response.getId());
        assertEquals("Test Product", response.getName());
        assertEquals(new BigDecimal("99.99"), response.getPrice());
        assertEquals(100, response.getStock());
        
        verify(productRepository).findById("product123");
    }

    @Test
    @DisplayName("Should throw exception when product not found by ID")
    void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById("nonexistent");
        });

        verify(productRepository).findById("nonexistent");
    }

    @Test
    @DisplayName("Should retrieve all products successfully")
    void testGetAllProducts_Success() {
        // Arrange
        Product product2 = new Product();
        product2.setId("product456");
        product2.setName("Another Product");
        product2.setPrice(new BigDecimal("49.99"));
        product2.setStock(50);

        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));

        // Act
        List<ProductResponse> products = productService.getAllProducts();

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals("Test Product", products.get(0).getName());
        assertEquals("Another Product", products.get(1).getName());
        
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should retrieve products by seller email")
    void testGetProductsBySeller_Success() {
        // Arrange
        Product product2 = new Product();
        product2.setId("product456");
        product2.setName("Seller Product 2");
        product2.setSellerEmail("seller@example.com");

        when(productRepository.findBySellerEmail("seller@example.com"))
                .thenReturn(Arrays.asList(testProduct, product2));

        // Act
        List<ProductResponse> products = productService.getProductsBySeller("seller@example.com");

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertTrue(products.stream().allMatch(p -> p.getSellerEmail().equals("seller@example.com")));
        
        verify(productRepository).findBySellerEmail("seller@example.com");
    }

    @Test
    @DisplayName("Should retrieve products by category")
    void testGetProductsByCategory_Success() {
        // Arrange
        Product product2 = new Product();
        product2.setId("product456");
        product2.setName("Electronics Item");
        product2.setCategory("Electronics");

        when(productRepository.findByCategory("Electronics"))
                .thenReturn(Arrays.asList(testProduct, product2));

        // Act
        List<ProductResponse> products = productService.getProductsByCategory("Electronics");

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertTrue(products.stream().allMatch(p -> p.getCategory().equals("Electronics")));
        
        verify(productRepository).findByCategory("Electronics");
    }

    @Test
    @DisplayName("Should delete product when seller owns it")
    void testDeleteProduct_Success() {
        // Arrange
        when(productRepository.findById("product123")).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).deleteById("product123");

        // Act
        productService.deleteProduct("product123", "seller@example.com");

        // Assert
        verify(productRepository).findById("product123");
        verify(productRepository).deleteById("product123");
    }

    @Test
    @DisplayName("Should throw exception when deleting product owned by another seller")
    void testDeleteProduct_Unauthorized() {
        // Arrange
        when(productRepository.findById("product123")).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            productService.deleteProduct("product123", "differentseller@example.com");
        });

        verify(productRepository).findById("product123");
        verify(productRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should update seller info for all products by seller")
    void testUpdateSellerInfo_Success() {
        // Arrange
        Product product2 = new Product();
        product2.setId("product456");
        product2.setSellerEmail("seller@example.com");
        
        List<Product> sellerProducts = Arrays.asList(testProduct, product2);
        when(productRepository.findBySellerEmail("seller@example.com")).thenReturn(sellerProducts);
        when(productRepository.saveAll(any())).thenReturn(sellerProducts);

        // Act
        productService.updateSellerInfo("seller@example.com", "Updated Name", "/new-avatar.jpg");

        // Assert
        verify(productRepository).findBySellerEmail("seller@example.com");
        verify(productRepository).saveAll(argThat(products -> {
            List<Product> productList = (List<Product>) products;
            return productList.size() == 2 &&
                   productList.stream().allMatch(p -> p.getSellerName().equals("Updated Name")) &&
                   productList.stream().allMatch(p -> p.getSellerAvatar().equals("/new-avatar.jpg"));
        }));
    }
}
