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

    // Test constants
    private static final String TEST_PRODUCT_ID = "product123";

    private static final String TEST_PRODUCT_NAME = "Test Product";

    private static final String TEST_DESCRIPTION = "Test Description";
    private static final BigDecimal TEST_PRICE = new BigDecimal("99.99");

    private static final int TEST_STOCK = 100;

    private static final String TEST_CATEGORY = "Electronics";
    private static final String TEST_SELLER_ID = "seller123";
    private static final String TEST_SELLER_EMAIL = "seller@example.com";
    private static final String TEST_SELLER_NAME = "John Seller";
    private static final String DIFFERENT_SELLER_EMAIL = "differentseller@example.com";
    private static final String NONEXISTENT_ID = "nonexistent";
    private static final String UPDATED_SELLER_NAME = "Updated Name";
    private static final String UPDATED_AVATAR = "/new-avatar.jpg";

    private static final BigDecimal TEST_PRICE_2 = new BigDecimal("49.99");
    private static final int TEST_STOCK_2 = 50;
    private static final String TEST_SELLER_AVATAR = "/avatars/seller.jpg";
    private static final String UPDATED_PRODUCT_NAME = "Updated Product";
    private static final String UPDATED_DESCRIPTION = "Updated Description";
    private static final String BOOKS_CATEGORY = "Books";
    private static final String SELLER_PRODUCT_2_NAME = "Seller Product 2";
    private static final String ELECTRONICS_ITEM_NAME = "Electronics Item";
    private static final String EMPTY_CATEGORY = "EmptyCategory";

    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(TEST_PRODUCT_ID);
        testProduct.setName(TEST_PRODUCT_NAME);
        testProduct.setDescription(TEST_DESCRIPTION);
        testProduct.setPrice(TEST_PRICE);
        testProduct.setStock(TEST_STOCK);
        testProduct.setCategory(TEST_CATEGORY);
        testProduct.setSellerId(TEST_SELLER_ID);
        testProduct.setSellerEmail(TEST_SELLER_EMAIL);
        testProduct.setSellerName(TEST_SELLER_NAME);

        // Initialize product request
        productRequest = new ProductRequest();
        productRequest.setName(TEST_PRODUCT_NAME);
        productRequest.setDescription(TEST_DESCRIPTION);
        productRequest.setPrice(TEST_PRICE);
        productRequest.setStock(TEST_STOCK);
        productRequest.setCategory(TEST_CATEGORY);
        productRequest.setSellerName(TEST_SELLER_NAME);
        productRequest.setSellerAvatar(TEST_SELLER_AVATAR);
    }

    @Test
    @DisplayName("Should successfully retrieve product by ID")
    void testGetProductByIdSuccess() {
        // Arrange
        when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(Optional.of(testProduct));

        // Act
        ProductResponse response = productService.getProductById(TEST_PRODUCT_ID);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_PRODUCT_ID, response.getId());
        assertEquals(TEST_PRODUCT_NAME, response.getName());
        assertEquals(TEST_PRICE, response.getPrice());
        assertEquals(TEST_STOCK, response.getStock());
        
        verify(productRepository).findById(TEST_PRODUCT_ID);
    }

    @Test
    @DisplayName("Should throw exception when product not found by ID")
    void testGetProductByIdNotFound() {
        // Arrange
        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            productService.getProductById(NONEXISTENT_ID)
        );

        verify(productRepository).findById(NONEXISTENT_ID);
    }

    @Test
    @DisplayName("Should retrieve all products successfully")
    void testGetAllProductsSuccess() {
        // Arrange
        Product product2 = new Product();
        product2.setId(TEST_PRODUCT_ID);
        product2.setName(TEST_PRODUCT_NAME);
        product2.setPrice(TEST_PRICE);
        product2.setStock(TEST_STOCK);

        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));

        // Act
        List<ProductResponse> products = productService.getAllProducts();

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals(TEST_PRODUCT_NAME, products.get(0).getName());
        assertEquals(TEST_PRODUCT_NAME, products.get(1).getName());
        
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should retrieve products by seller email")
    void testGetProductsBySellerSuccess() {
        // Arrange
        Product product2 = new Product();
        product2.setId(TEST_PRODUCT_ID);
        product2.setName(SELLER_PRODUCT_2_NAME);
        product2.setSellerEmail(TEST_SELLER_EMAIL);

        when(productRepository.findBySellerEmail(TEST_SELLER_EMAIL))
                .thenReturn(Arrays.asList(testProduct, product2));

        // Act
        List<ProductResponse> products = productService.getProductsBySeller(TEST_SELLER_EMAIL);

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertTrue(products.stream().allMatch(p -> p.getSellerEmail().equals(TEST_SELLER_EMAIL)));
        
        verify(productRepository).findBySellerEmail(TEST_SELLER_EMAIL);
    }

    @Test
    @DisplayName("Should retrieve products by category")
    void testGetProductsByCategorySuccess() {
        // Arrange
        Product product2 = new Product();
        product2.setId(TEST_PRODUCT_ID);
        product2.setName(ELECTRONICS_ITEM_NAME);
        product2.setCategory(TEST_CATEGORY);

        when(productRepository.findByCategory(TEST_CATEGORY))
                .thenReturn(Arrays.asList(testProduct, product2));

        // Act
        List<ProductResponse> products = productService.getProductsByCategory(TEST_CATEGORY);

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertTrue(products.stream().allMatch(p -> p.getCategory().equals(TEST_CATEGORY)));
        
        verify(productRepository).findByCategory(TEST_CATEGORY);
    }

    @Test
    @DisplayName("Should delete product when seller owns it")
    void testDeleteProductSuccess() {
        // Arrange
        when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).deleteById(TEST_PRODUCT_ID);

        // Act
        productService.deleteProduct(TEST_PRODUCT_ID, TEST_SELLER_EMAIL);

        // Assert
        verify(productRepository).findById(TEST_PRODUCT_ID);
        verify(productRepository).deleteById(TEST_PRODUCT_ID);
    }

    @Test
    @DisplayName("Should throw exception when deleting product owned by another seller")
    void testDeleteProductUnauthorized() {
        // Arrange
        when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            productService.deleteProduct(TEST_PRODUCT_ID, DIFFERENT_SELLER_EMAIL)
        );

        verify(productRepository).findById(TEST_PRODUCT_ID);
        verify(productRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should update seller info for all products by seller")
    void testUpdateSellerInfoSuccess() {
        // Arrange
        Product product2 = new Product();
        product2.setId(TEST_PRODUCT_ID);
        product2.setSellerEmail(TEST_SELLER_EMAIL);
        
        List<Product> sellerProducts = Arrays.asList(testProduct, product2);
        when(productRepository.findBySellerEmail(TEST_SELLER_EMAIL)).thenReturn(sellerProducts);
        when(productRepository.saveAll(any())).thenReturn(sellerProducts);

        // Act
        productService.updateSellerInfo(TEST_SELLER_EMAIL, UPDATED_SELLER_NAME, UPDATED_AVATAR);

        // Assert
        verify(productRepository).findBySellerEmail(TEST_SELLER_EMAIL);
        verify(productRepository).saveAll(argThat(products -> {
            List<Product> productList = (List<Product>) products;
            return productList.size() == 2 &&
                   productList.stream().allMatch(p -> p.getSellerName().equals(UPDATED_SELLER_NAME)) &&
                   productList.stream().allMatch(p -> p.getSellerAvatar().equals(UPDATED_AVATAR));
        }));
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProductSuccess() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse response = productService.createProduct(productRequest, TEST_SELLER_EMAIL, TEST_SELLER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_PRODUCT_NAME, response.getName());
        assertEquals(TEST_DESCRIPTION, response.getDescription());
        assertEquals(TEST_PRICE, response.getPrice());
        assertEquals(TEST_STOCK, response.getStock());
        assertEquals(TEST_CATEGORY, response.getCategory());
        assertEquals(TEST_SELLER_EMAIL, response.getSellerEmail());
        
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when non-seller tries to create product")
    void testCreateProductUnauthorized() {
        // Arrange - using empty email should fail seller verification
        String emptyEmail = "";

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            productService.createProduct(productRequest, emptyEmail, TEST_SELLER_ID)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProductSuccess() {
        // Arrange
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName(UPDATED_PRODUCT_NAME);
        updateRequest.setDescription(UPDATED_DESCRIPTION);
        updateRequest.setPrice(TEST_PRICE_2);
        updateRequest.setStock(TEST_STOCK_2);
        updateRequest.setCategory(BOOKS_CATEGORY);

        Product updatedProduct = new Product();
        updatedProduct.setId(TEST_PRODUCT_ID);
        updatedProduct.setName(UPDATED_PRODUCT_NAME);
        updatedProduct.setDescription(UPDATED_DESCRIPTION);
        updatedProduct.setPrice(TEST_PRICE_2);
        updatedProduct.setStock(TEST_STOCK_2);
        updatedProduct.setCategory(BOOKS_CATEGORY);
        updatedProduct.setSellerEmail(TEST_SELLER_EMAIL);

        when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ProductResponse response = productService.updateProduct(TEST_PRODUCT_ID, updateRequest, TEST_SELLER_EMAIL);

        // Assert
        assertNotNull(response);
        assertEquals(UPDATED_PRODUCT_NAME, response.getName());
        assertEquals(UPDATED_DESCRIPTION, response.getDescription());
        assertEquals(TEST_PRICE_2, response.getPrice());
        assertEquals(TEST_STOCK_2, response.getStock());
        assertEquals(BOOKS_CATEGORY, response.getCategory());
        
        verify(productRepository).findById(TEST_PRODUCT_ID);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating product not found")
    void testUpdateProductNotFound() {
        // Arrange
        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            productService.updateProduct(NONEXISTENT_ID, productRequest, TEST_SELLER_EMAIL)
        );

        verify(productRepository).findById(NONEXISTENT_ID);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating product owned by another seller")
    void testUpdateProductUnauthorized() {
        // Arrange
        when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            productService.updateProduct(TEST_PRODUCT_ID, productRequest, DIFFERENT_SELLER_EMAIL)
        );

        verify(productRepository).findById(TEST_PRODUCT_ID);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void testGetAllProductsEmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ProductResponse> products = productService.getAllProducts();

        // Assert
        assertNotNull(products);
        assertTrue(products.isEmpty());
        
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when seller has no products")
    void testGetProductsBySellerEmptyList() {
        // Arrange
        when(productRepository.findBySellerEmail(TEST_SELLER_EMAIL)).thenReturn(Arrays.asList());

        // Act
        List<ProductResponse> products = productService.getProductsBySeller(TEST_SELLER_EMAIL);

        // Assert
        assertNotNull(products);
        assertTrue(products.isEmpty());
        
        verify(productRepository).findBySellerEmail(TEST_SELLER_EMAIL);
    }

    @Test
    @DisplayName("Should return empty list when category has no products")
    void testGetProductsByCategoryEmptyList() {
        // Arrange
        when(productRepository.findByCategory(EMPTY_CATEGORY)).thenReturn(Arrays.asList());

        // Act
        List<ProductResponse> products = productService.getProductsByCategory(EMPTY_CATEGORY);

        // Assert
        assertNotNull(products);
        assertTrue(products.isEmpty());
        
        verify(productRepository).findByCategory(EMPTY_CATEGORY);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void testDeleteProductNotFound() {
        // Arrange
        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            productService.deleteProduct(NONEXISTENT_ID, TEST_SELLER_EMAIL)
        );

        verify(productRepository).findById(NONEXISTENT_ID);
        verify(productRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should handle null seller email in updateSellerInfo")
    void testUpdateSellerInfoNoProducts() {
        // Arrange
        when(productRepository.findBySellerEmail(TEST_SELLER_EMAIL)).thenReturn(Arrays.asList());

        // Act
        productService.updateSellerInfo(TEST_SELLER_EMAIL, UPDATED_SELLER_NAME, UPDATED_AVATAR);

        // Assert
        verify(productRepository).findBySellerEmail(TEST_SELLER_EMAIL);
        verify(productRepository).saveAll(argThat(products -> {
            List<Product> productList = (List<Product>) products;
            return productList.isEmpty();
        }));
    }
}
