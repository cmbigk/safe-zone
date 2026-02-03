package com.ecommerce.productservice.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private static final String ID = "product-1";
    private static final String NAME = "Laptop";
    private static final String DESCRIPTION = "High-performance laptop";
    private static final BigDecimal PRICE = new BigDecimal("999.99");
    private static final Integer STOCK = 10;
    private static final String CATEGORY = "Electronics";
    private static final String SELLER_ID = "seller-1";
    private static final String SELLER_EMAIL = "seller@example.com";
    private static final String SELLER_NAME = "John Doe";
    private static final String SELLER_AVATAR = "avatar.jpg";
    private static final List<String> IMAGE_IDS = Arrays.asList("img1", "img2");
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final LocalDateTime UPDATED_AT = LocalDateTime.now();

    @Test
    void testNoArgsConstructor() {
        Product product = new Product();
        assertNotNull(product);
        assertNotNull(product.getImageIds()); // Default empty list
    }

    @Test
    void testAllArgsConstructor() {
        Product product = new Product(
                ID, NAME, DESCRIPTION, PRICE, STOCK, CATEGORY,
                SELLER_ID, SELLER_EMAIL, SELLER_NAME, SELLER_AVATAR,
                IMAGE_IDS, CREATED_AT, UPDATED_AT
        );

        assertEquals(ID, product.getId());
        assertEquals(NAME, product.getName());
        assertEquals(DESCRIPTION, product.getDescription());
        assertEquals(PRICE, product.getPrice());
        assertEquals(STOCK, product.getStock());
        assertEquals(CATEGORY, product.getCategory());
        assertEquals(SELLER_ID, product.getSellerId());
        assertEquals(SELLER_EMAIL, product.getSellerEmail());
        assertEquals(SELLER_NAME, product.getSellerName());
        assertEquals(SELLER_AVATAR, product.getSellerAvatar());
        assertEquals(IMAGE_IDS, product.getImageIds());
        assertEquals(CREATED_AT, product.getCreatedAt());
        assertEquals(UPDATED_AT, product.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        Product product = new Product();

        product.setId(ID);
        product.setName(NAME);
        product.setDescription(DESCRIPTION);
        product.setPrice(PRICE);
        product.setStock(STOCK);
        product.setCategory(CATEGORY);
        product.setSellerId(SELLER_ID);
        product.setSellerEmail(SELLER_EMAIL);
        product.setSellerName(SELLER_NAME);
        product.setSellerAvatar(SELLER_AVATAR);
        product.setImageIds(IMAGE_IDS);
        product.setCreatedAt(CREATED_AT);
        product.setUpdatedAt(UPDATED_AT);

        assertEquals(ID, product.getId());
        assertEquals(NAME, product.getName());
        assertEquals(DESCRIPTION, product.getDescription());
        assertEquals(PRICE, product.getPrice());
        assertEquals(STOCK, product.getStock());
        assertEquals(CATEGORY, product.getCategory());
        assertEquals(SELLER_ID, product.getSellerId());
        assertEquals(SELLER_EMAIL, product.getSellerEmail());
        assertEquals(SELLER_NAME, product.getSellerName());
        assertEquals(SELLER_AVATAR, product.getSellerAvatar());
        assertEquals(IMAGE_IDS, product.getImageIds());
        assertEquals(CREATED_AT, product.getCreatedAt());
        assertEquals(UPDATED_AT, product.getUpdatedAt());
    }

    @Test
    void testImageIdsList() {
        Product product = new Product();
        assertNotNull(product.getImageIds());
        assertTrue(product.getImageIds().isEmpty());

        product.setImageIds(IMAGE_IDS);
        assertEquals(2, product.getImageIds().size());
        assertTrue(product.getImageIds().contains("img1"));
        assertTrue(product.getImageIds().contains("img2"));
    }
}
