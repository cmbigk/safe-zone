package com.ecommerce.productservice.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductRequestTest {

    private static final String PRODUCT_NAME = "Test Product";
    private static final String DESCRIPTION = "Test Description with more than 10 characters";
    private static final BigDecimal PRICE = new BigDecimal("99.99");
    private static final Integer STOCK = 10;
    private static final String CATEGORY = "Electronics";
    private static final String SELLER_NAME = "Test Seller";
    private static final String SELLER_AVATAR = "avatar.jpg";
    private static final List<String> IMAGE_IDS = Arrays.asList("img1", "img2");

    @Test
    void testNoArgsConstructor() {
        ProductRequest request = new ProductRequest();
        assertNotNull(request);
    }

    @Test
    void testAllArgsConstructor() {
        ProductRequest request = new ProductRequest(
                PRODUCT_NAME, DESCRIPTION, PRICE, STOCK, CATEGORY, SELLER_NAME, IMAGE_IDS
        );
        
        assertEquals(PRODUCT_NAME, request.getName());
        assertEquals(DESCRIPTION, request.getDescription());
        assertEquals(PRICE, request.getPrice());
        assertEquals(STOCK, request.getStock());
        assertEquals(CATEGORY, request.getCategory());
        assertEquals(SELLER_NAME, request.getSellerName());
        assertEquals(IMAGE_IDS, request.getImageIds());
    }

    @Test
    void testSettersAndGetters() {
        ProductRequest request = new ProductRequest();
        
        request.setName(PRODUCT_NAME);
        request.setDescription(DESCRIPTION);
        request.setPrice(PRICE);
        request.setStock(STOCK);
        request.setCategory(CATEGORY);
        request.setSellerName(SELLER_NAME);
        request.setSellerAvatar(SELLER_AVATAR);
        request.setImageIds(IMAGE_IDS);
        
        assertEquals(PRODUCT_NAME, request.getName());
        assertEquals(DESCRIPTION, request.getDescription());
        assertEquals(PRICE, request.getPrice());
        assertEquals(STOCK, request.getStock());
        assertEquals(CATEGORY, request.getCategory());
        assertEquals(SELLER_NAME, request.getSellerName());
        assertEquals(SELLER_AVATAR, request.getSellerAvatar());
        assertEquals(IMAGE_IDS, request.getImageIds());
    }
}
