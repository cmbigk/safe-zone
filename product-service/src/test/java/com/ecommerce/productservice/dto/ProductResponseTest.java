package com.ecommerce.productservice.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductResponseTest {

    private static final String ID = "product-1";
    private static final String PRODUCT_NAME = "Test Product";
    private static final String DESCRIPTION = "Test Description";
    private static final BigDecimal PRICE = new BigDecimal("99.99");
    private static final Integer STOCK = 10;
    private static final String CATEGORY = "Electronics";
    private static final String SELLER_ID = "seller-1";
    private static final String SELLER_EMAIL = "seller@example.com";
    private static final String SELLER_NAME = "Test Seller";
    private static final String SELLER_AVATAR = "avatar.jpg";
    private static final List<String> IMAGE_IDS = Arrays.asList("img1", "img2");
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final LocalDateTime UPDATED_AT = LocalDateTime.now();

    @Test
    void testNoArgsConstructor() {
        ProductResponse response = new ProductResponse();
        assertNotNull(response);
    }

    @Test
    void testAllArgsConstructor() {
        ProductResponse response = new ProductResponse(
                ID, PRODUCT_NAME, DESCRIPTION, PRICE, STOCK, CATEGORY,
                SELLER_ID, SELLER_EMAIL, SELLER_NAME, SELLER_AVATAR,
                IMAGE_IDS, CREATED_AT, UPDATED_AT
        );
        
        assertEquals(ID, response.getId());
        assertEquals(PRODUCT_NAME, response.getName());
        assertEquals(DESCRIPTION, response.getDescription());
        assertEquals(PRICE, response.getPrice());
        assertEquals(STOCK, response.getStock());
        assertEquals(CATEGORY, response.getCategory());
        assertEquals(SELLER_ID, response.getSellerId());
        assertEquals(SELLER_EMAIL, response.getSellerEmail());
        assertEquals(SELLER_NAME, response.getSellerName());
        assertEquals(SELLER_AVATAR, response.getSellerAvatar());
        assertEquals(IMAGE_IDS, response.getImageIds());
        assertEquals(CREATED_AT, response.getCreatedAt());
        assertEquals(UPDATED_AT, response.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        ProductResponse response = new ProductResponse();
        
        response.setId(ID);
        response.setName(PRODUCT_NAME);
        response.setDescription(DESCRIPTION);
        response.setPrice(PRICE);
        response.setStock(STOCK);
        response.setCategory(CATEGORY);
        response.setSellerId(SELLER_ID);
        response.setSellerEmail(SELLER_EMAIL);
        response.setSellerName(SELLER_NAME);
        response.setSellerAvatar(SELLER_AVATAR);
        response.setImageIds(IMAGE_IDS);
        response.setCreatedAt(CREATED_AT);
        response.setUpdatedAt(UPDATED_AT);
        
        assertEquals(ID, response.getId());
        assertEquals(PRODUCT_NAME, response.getName());
        assertEquals(DESCRIPTION, response.getDescription());
        assertEquals(PRICE, response.getPrice());
        assertEquals(STOCK, response.getStock());
        assertEquals(CATEGORY, response.getCategory());
        assertEquals(SELLER_ID, response.getSellerId());
        assertEquals(SELLER_EMAIL, response.getSellerEmail());
        assertEquals(SELLER_NAME, response.getSellerName());
        assertEquals(SELLER_AVATAR, response.getSellerAvatar());
        assertEquals(IMAGE_IDS, response.getImageIds());
        assertEquals(CREATED_AT, response.getCreatedAt());
        assertEquals(UPDATED_AT, response.getUpdatedAt());
    }
}
