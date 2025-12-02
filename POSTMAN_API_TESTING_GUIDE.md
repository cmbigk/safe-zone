# Postman API Testing Guide

## Prerequisites

1. **Import Collection & Environment**
   - Import `postman-collection.json` into Postman
   - Import `postman-environment.json` and set it as active environment
   - Ensure Docker containers are running: `docker-compose ps`

2. **Base URL**
   - User Service: `http://localhost:8081`
   - All endpoints start with `/api/auth` or `/api/users`

---

## Test Sequence

### 1. Register a Seller Account

**Endpoint:** `POST http://localhost:8081/api/auth/register`

**Request Body:**
```json
{
  "email": "john.seller@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Seller",
  "phone": "+1234567890",
  "role": "SELLER"
}
```

**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "691877ea124c7c1fc73ae78f",
  "email": "john.seller@example.com",
  "role": "SELLER"
}
```

**Postman Test Script (Auto-saves token):**
```javascript
if (pm.response.code === 201) {
    pm.environment.set("seller_token", pm.response.json().token);
    pm.environment.set("seller_id", pm.response.json().id);
}
```

---

### 2. Register a Client Account

**Endpoint:** `POST http://localhost:8081/api/auth/register`

**Request Body:**
```json
{
  "email": "jane.client@example.com",
  "password": "ClientPass456!",
  "firstName": "Jane",
  "lastName": "Client",
  "phone": "+1987654321",
  "role": "CLIENT"
}
```

**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "691877ea124c7c1fc73ae78g",
  "email": "jane.client@example.com",
  "role": "CLIENT"
}
```

**Postman Test Script:**
```javascript
if (pm.response.code === 201) {
    pm.environment.set("client_token", pm.response.json().token);
    pm.environment.set("client_id", pm.response.json().id);
}
```

---

### 3. Login as Seller

**Endpoint:** `POST http://localhost:8081/api/auth/login`

**Request Body:**
```json
{
  "email": "john.seller@example.com",
  "password": "SecurePass123!"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "691877ea124c7c1fc73ae78f",
  "email": "john.seller@example.com",
  "role": "SELLER"
}
```

---

### 4. Login as Client

**Endpoint:** `POST http://localhost:8081/api/auth/login`

**Request Body:**
```json
{
  "email": "jane.client@example.com",
  "password": "ClientPass456!"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "691877ea124c7c1fc73ae78g",
  "email": "jane.client@example.com",
  "role": "CLIENT"
}
```

---

### 5. Get Seller Profile

**Endpoint:** `GET http://localhost:8081/api/auth/profile`

**Headers:**
```
Authorization: Bearer {{seller_token}}
```

**Expected Response (200 OK):**
```json
{
  "id": "691877ea124c7c1fc73ae78f",
  "email": "john.seller@example.com",
  "firstName": "John",
  "lastName": "Seller",
  "phone": "+1234567890",
  "role": "SELLER",
  "avatarUrl": null,
  "createdAt": "2025-11-15T12:34:56",
  "updatedAt": "2025-11-15T12:34:56"
}
```

---

### 6. Get Client Profile

**Endpoint:** `GET http://localhost:8081/api/auth/profile`

**Headers:**
```
Authorization: Bearer {{client_token}}
```

**Expected Response (200 OK):**
```json
{
  "id": "691877ea124c7c1fc73ae78g",
  "email": "jane.client@example.com",
  "firstName": "Jane",
  "lastName": "Client",
  "phone": "+1987654321",
  "role": "CLIENT",
  "avatarUrl": null,
  "createdAt": "2025-11-15T12:35:12",
  "updatedAt": "2025-11-15T12:35:12"
}
```

---

### 7. Update Seller Profile

**Endpoint:** `PUT http://localhost:8081/api/auth/profile`

**Headers:**
```
Authorization: Bearer {{seller_token}}
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Seller Updated",
  "phone": "+1234567899"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "691877ea124c7c1fc73ae78f",
  "email": "john.seller@example.com",
  "firstName": "John",
  "lastName": "Seller Updated",
  "phone": "+1234567899",
  "role": "SELLER",
  "avatarUrl": null,
  "createdAt": "2025-11-15T12:34:56",
  "updatedAt": "2025-11-15T12:36:45"
}
```

---

### 8. Upload Seller Avatar

**Endpoint:** `POST http://localhost:8081/api/auth/avatar`

**Headers:**
```
Authorization: Bearer {{seller_token}}
```

**Body Type:** `form-data`

**Form Data:**
- Key: `file` (Type: File)
- Value: Select an image file (JPG, PNG, GIF - max 2MB)

**Expected Response (200 OK):**
```json
{
  "message": "Avatar uploaded successfully",
  "avatarUrl": "/uploads/avatars/550e8400-e29b-41d4-a716-446655440000.jpg"
}
```

---

### 9. Get User by ID (Admin/Self)

**Endpoint:** `GET http://localhost:8081/api/users/{{seller_id}}`

**Headers:**
```
Authorization: Bearer {{seller_token}}
```

**Expected Response (200 OK):**
```json
{
  "id": "691877ea124c7c1fc73ae78f",
  "email": "john.seller@example.com",
  "firstName": "John",
  "lastName": "Seller Updated",
  "phone": "+1234567899",
  "role": "SELLER",
  "avatarUrl": "/uploads/avatars/550e8400-e29b-41d4-a716-446655440000.jpg",
  "createdAt": "2025-11-15T12:34:56",
  "updatedAt": "2025-11-15T12:37:23"
}
```

---

## Error Testing

### 10. Register with Duplicate Email

**Endpoint:** `POST http://localhost:8081/api/auth/register`

**Request Body:**
```json
{
  "email": "john.seller@example.com",
  "password": "AnotherPass789!",
  "firstName": "John",
  "lastName": "Duplicate",
  "phone": "+1111111111",
  "role": "SELLER"
}
```

**Expected Response (409 Conflict):**
```json
{
  "status": 409,
  "message": "User with email john.seller@example.com already exists",
  "timestamp": "2025-11-15T12:38:15"
}
```

---

### 11. Login with Invalid Credentials

**Endpoint:** `POST http://localhost:8081/api/auth/login`

**Request Body:**
```json
{
  "email": "john.seller@example.com",
  "password": "WrongPassword123!"
}
```

**Expected Response (401 Unauthorized):**
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "timestamp": "2025-11-15T12:38:45"
}
```

---

### 12. Access Protected Endpoint Without Token

**Endpoint:** `GET http://localhost:8081/api/auth/profile`

**Headers:** (No Authorization header)

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "Access Denied",
  "timestamp": "2025-11-15T12:39:12"
}
```

---

### 13. Register with Invalid Email Format

**Endpoint:** `POST http://localhost:8081/api/auth/register`

**Request Body:**
```json
{
  "email": "invalid-email",
  "password": "ValidPass123!",
  "firstName": "Test",
  "lastName": "User",
  "phone": "+1234567890",
  "role": "CLIENT"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email should be valid"
  },
  "timestamp": "2025-11-15T12:39:45"
}
```

---

### 14. Register with Weak Password

**Endpoint:** `POST http://localhost:8081/api/auth/register`

**Request Body:**
```json
{
  "email": "test@example.com",
  "password": "123",
  "firstName": "Test",
  "lastName": "User",
  "phone": "+1234567890",
  "role": "CLIENT"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2025-11-15T12:40:15"
}
```

---

### 15. Upload Oversized Avatar

**Endpoint:** `POST http://localhost:8081/api/auth/avatar`

**Headers:**
```
Authorization: Bearer {{seller_token}}
```

**Body:** Upload a file larger than 2MB

**Expected Response (413 Payload Too Large):**
```json
{
  "status": 413,
  "message": "File size exceeds maximum limit of 2MB",
  "timestamp": "2025-11-15T12:40:45"
}
```

---

### 16. Upload Invalid File Type

**Endpoint:** `POST http://localhost:8081/api/auth/avatar`

**Headers:**
```
Authorization: Bearer {{seller_token}}
```

**Body:** Upload a non-image file (e.g., .pdf, .txt)

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Only image files (jpg, jpeg, png, gif) are allowed",
  "timestamp": "2025-11-15T12:41:15"
}
```

---

### 17. Access Non-Existent User

**Endpoint:** `GET http://localhost:8081/api/users/000000000000000000000000`

**Headers:**
```
Authorization: Bearer {{seller_token}}
```

**Expected Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "User not found with id: 000000000000000000000000",
  "timestamp": "2025-11-15T12:41:45"
}
```

---

## Postman Environment Variables

After running the tests, your environment should have these variables:

```javascript
{
  "base_url": "http://localhost:8081",
  "seller_token": "eyJhbGciOiJIUzUxMiJ9...",
  "client_token": "eyJhbGciOiJIUzUxMiJ9...",
  "seller_id": "691877ea124c7c1fc73ae78f",
  "client_id": "691877ea124c7c1fc73ae78g",
  "seller_email": "john.seller@example.com",
  "client_email": "jane.client@example.com"
}
```

---

## Tips for Testing

1. **Run Tests in Sequence**: Start with registration, then login, then protected endpoints
2. **Check Environment Variables**: Ensure tokens are auto-saved after registration/login
3. **Token Expiration**: Tokens expire after 24 hours - re-login if you get 401 errors
4. **Clean Database**: To reset, run `docker-compose down -v` then `docker-compose up -d`
5. **View Logs**: Check logs with `docker logs user-service -f` if unexpected errors occur

---

## Quick Test Script

Run all tests automatically using Postman's Collection Runner:

1. Click **Collections** → Select **E-Commerce User Service**
2. Click **Run** button
3. Select the environment: **E-Commerce Environment**
4. Click **Run E-Commerce User Service**
5. View results in the runner window

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection refused | Check if containers are running: `docker-compose ps` |
| 401 Unauthorized | Token expired - login again to get new token |
| 500 Internal Server Error | Check logs: `docker logs user-service` |
| MongoDB connection failed | Restart MongoDB: `docker-compose restart mongodb-user` |
| Port already in use | Stop conflicting service or change port in `docker-compose.yml` |

---

## Success Criteria

✅ All registration endpoints return 201 with valid JWT tokens  
✅ Login endpoints authenticate correctly and return tokens  
✅ Profile endpoints return user data when authenticated  
✅ Update endpoints modify user data successfully  
✅ Avatar upload accepts valid images and rejects invalid ones  
✅ Error endpoints return appropriate HTTP status codes  
✅ Validation errors provide clear error messages  
✅ Unauthorized access is properly blocked  

---

## Next Steps

Once User Service testing is complete:
1. Test Product Microservice (when implemented)
2. Test Media Microservice (when implemented)
3. Test API Gateway routing
4. Test Angular Frontend integration

---

---

# Product Service API Testing

## Prerequisites

1. **Complete User Service Tests First**
   - You need valid `seller_token` and `seller_id` from User Service registration
   - Ensure Product Service is running: `docker ps | Select-String product-service`

2. **Base URL**
   - Product Service: `http://localhost:8082`
   - All endpoints start with `/api/products`

---

## Product Test Sequence

### 18. Create Product (Seller Only)

**Endpoint:** `POST http://localhost:8082/api/products`

**Headers:**
```
Authorization: Bearer {{seller_token}}
X-User-Email: john.seller@example.com
X-User-Id: {{seller_id}}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Wireless Gaming Mouse",
  "description": "High-precision wireless gaming mouse with RGB lighting and 16000 DPI sensor",
  "price": 79.99,
  "stock": 50,
  "category": "Electronics",
  "imageIds": []
}
```

**Expected Response (201 Created):**
```json
{
  "id": "691877ea124c7c1fc73ae790",
  "name": "Wireless Gaming Mouse",
  "description": "High-precision wireless gaming mouse with RGB lighting and 16000 DPI sensor",
  "price": 79.99,
  "stock": 50,
  "category": "Electronics",
  "sellerEmail": "john.seller@example.com",
  "sellerId": "691877ea124c7c1fc73ae78f",
  "imageIds": [],
  "createdAt": "2025-11-15T13:00:00",
  "updatedAt": "2025-11-15T13:00:00"
}
```

**Postman Test Script:**
```javascript
if (pm.response.code === 201) {
    pm.environment.set("product_id", pm.response.json().id);
}
```

---

### 19. Get All Products (Public Access)

**Endpoint:** `GET http://localhost:8082/api/products`

**Headers:** (No authentication required)

**Expected Response (200 OK):**
```json
[
  {
    "id": "691877ea124c7c1fc73ae790",
    "name": "Wireless Gaming Mouse",
    "description": "High-precision wireless gaming mouse with RGB lighting and 16000 DPI sensor",
    "price": 79.99,
    "stock": 50,
    "category": "Electronics",
    "sellerEmail": "john.seller@example.com",
    "sellerId": "691877ea124c7c1fc73ae78f",
    "imageIds": [],
    "createdAt": "2025-11-15T13:00:00",
    "updatedAt": "2025-11-15T13:00:00"
  }
]
```

---

### 20. Get Product by ID (Public Access)

**Endpoint:** `GET http://localhost:8082/api/products/{{product_id}}`

**Headers:** (No authentication required)

**Expected Response (200 OK):**
```json
{
  "id": "691877ea124c7c1fc73ae790",
  "name": "Wireless Gaming Mouse",
  "description": "High-precision wireless gaming mouse with RGB lighting and 16000 DPI sensor",
  "price": 79.99,
  "stock": 50,
  "category": "Electronics",
  "sellerEmail": "john.seller@example.com",
  "sellerId": "691877ea124c7c1fc73ae78f",
  "imageIds": [],
  "createdAt": "2025-11-15T13:00:00",
  "updatedAt": "2025-11-15T13:00:00"
}
```

---

### 21. Get Products by Seller (Public Access)

**Endpoint:** `GET http://localhost:8082/api/products/seller/john.seller@example.com`

**Headers:** (No authentication required)

**Expected Response (200 OK):**
```json
[
  {
    "id": "691877ea124c7c1fc73ae790",
    "name": "Wireless Gaming Mouse",
    "description": "High-precision wireless gaming mouse with RGB lighting and 16000 DPI sensor",
    "price": 79.99,
    "stock": 50,
    "category": "Electronics",
    "sellerEmail": "john.seller@example.com",
    "sellerId": "691877ea124c7c1fc73ae78f",
    "imageIds": [],
    "createdAt": "2025-11-15T13:00:00",
    "updatedAt": "2025-11-15T13:00:00"
  }
]
```

---

### 22. Get Products by Category (Public Access)

**Endpoint:** `GET http://localhost:8082/api/products/category/Electronics`

**Headers:** (No authentication required)

**Expected Response (200 OK):**
```json
[
  {
    "id": "691877ea124c7c1fc73ae790",
    "name": "Wireless Gaming Mouse",
    "description": "High-precision wireless gaming mouse with RGB lighting and 16000 DPI sensor",
    "price": 79.99,
    "stock": 50,
    "category": "Electronics",
    "sellerEmail": "john.seller@example.com",
    "sellerId": "691877ea124c7c1fc73ae78f",
    "imageIds": [],
    "createdAt": "2025-11-15T13:00:00",
    "updatedAt": "2025-11-15T13:00:00"
  }
]
```

---

### 23. Update Product (Owner Only)

**Endpoint:** `PUT http://localhost:8082/api/products/{{product_id}}`

**Headers:**
```
Authorization: Bearer {{seller_token}}
X-User-Email: john.seller@example.com
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Wireless Gaming Mouse Pro",
  "description": "High-precision wireless gaming mouse with RGB lighting, 16000 DPI sensor, and programmable buttons",
  "price": 89.99,
  "stock": 45,
  "category": "Electronics",
  "imageIds": []
}
```

**Expected Response (200 OK):**
```json
{
  "id": "691877ea124c7c1fc73ae790",
  "name": "Wireless Gaming Mouse Pro",
  "description": "High-precision wireless gaming mouse with RGB lighting, 16000 DPI sensor, and programmable buttons",
  "price": 89.99,
  "stock": 45,
  "category": "Electronics",
  "sellerEmail": "john.seller@example.com",
  "sellerId": "691877ea124c7c1fc73ae78f",
  "imageIds": [],
  "createdAt": "2025-11-15T13:00:00",
  "updatedAt": "2025-11-15T13:05:23"
}
```

---

### 24. Delete Product (Owner Only)

**Endpoint:** `DELETE http://localhost:8082/api/products/{{product_id}}`

**Headers:**
```
Authorization: Bearer {{seller_token}}
X-User-Email: john.seller@example.com
```

**Expected Response (204 No Content)**

---

## Product Error Testing

### 25. Create Product Without Authentication

**Endpoint:** `POST http://localhost:8082/api/products`

**Headers:** (No Authorization header)

**Request Body:**
```json
{
  "name": "Test Product",
  "description": "This should fail without authentication",
  "price": 29.99,
  "stock": 10,
  "category": "Test"
}
```

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "Access Denied",
  "timestamp": "2025-11-15T13:10:00"
}
```

---

### 26. Create Product with Invalid Data

**Endpoint:** `POST http://localhost:8082/api/products`

**Headers:**
```
Authorization: Bearer {{seller_token}}
X-User-Email: john.seller@example.com
X-User-Id: {{seller_id}}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "AB",
  "description": "Too short",
  "price": -10.00,
  "stock": -5,
  "category": ""
}
```

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "name": "Product name must be between 3 and 100 characters",
    "description": "Description must be between 10 and 1000 characters",
    "price": "Price must be greater than 0",
    "stock": "Stock cannot be negative",
    "category": "Category is required"
  },
  "timestamp": "2025-11-15T13:12:00"
}
```

---

### 27. Update Product as Different Seller (Authorization Test)

**Endpoint:** `PUT http://localhost:8082/api/products/{{product_id}}`

**Headers:**
```
Authorization: Bearer {{client_token}}
X-User-Email: jane.client@example.com
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Unauthorized Update",
  "description": "This should fail because client is not the owner",
  "price": 99.99,
  "stock": 100,
  "category": "Electronics"
}
```

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "You are not authorized to update this product",
  "timestamp": "2025-11-15T13:15:00"
}
```

---

### 28. Get Non-Existent Product

**Endpoint:** `GET http://localhost:8082/api/products/000000000000000000000000`

**Headers:** (No authentication required)

**Expected Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Product not found with id: 000000000000000000000000",
  "timestamp": "2025-11-15T13:16:00"
}
```

---

### 29. Delete Product as Non-Owner

**Endpoint:** `DELETE http://localhost:8082/api/products/{{product_id}}`

**Headers:**
```
Authorization: Bearer {{client_token}}
X-User-Email: jane.client@example.com
```

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "You are not authorized to delete this product",
  "timestamp": "2025-11-15T13:18:00"
}
```

---

## Updated Environment Variables

After running product tests, add these variables:

```javascript
{
  "base_url": "http://localhost:8081",
  "product_service_url": "http://localhost:8082",
  "seller_token": "eyJhbGciOiJIUzUxMiJ9...",
  "client_token": "eyJhbGciOiJIUzUxMiJ9...",
  "seller_id": "691877ea124c7c1fc73ae78f",
  "client_id": "691877ea124c7c1fc73ae78g",
  "seller_email": "john.seller@example.com",
  "client_email": "jane.client@example.com",
  "product_id": "691877ea124c7c1fc73ae790"
}
```

---

## Product Testing Success Criteria

✅ Sellers can create products with valid data  
✅ Public users can view all products without authentication  
✅ Products can be filtered by seller email  
✅ Products can be filtered by category  
✅ Only product owners can update their products  
✅ Only product owners can delete their products  
✅ Validation errors provide clear feedback  
✅ Unauthorized access attempts are blocked  
✅ Non-existent products return 404 errors  

---

## Complete Test Flow

1. **Register Seller** (User Service)
2. **Login as Seller** → Get JWT token
3. **Create Product** → Save product_id
4. **View All Products** (public)
5. **View Product by ID** (public)
6. **Filter by Seller** (public)
7. **Filter by Category** (public)
8. **Update Product** (owner only)
9. **Register Client** (User Service)
10. **Try to Update Product as Client** → Should fail (403)
11. **Try to Delete Product as Client** → Should fail (403)
12. **Delete Product as Owner** → Success (204)

---

**Happy Testing! 🚀**
