# E-Commerce Platform - API Testing & Audit Checklist

## Initial Setup

### 1. Start All Services
```powershell
cd 'C:\Users\HP Victus 15\Desktop\buy-01'
docker-compose up --build
```

Wait for all services to start:
- MongoDB (user, product, media): ports 27017, 27018, 27019
- Kafka & Zookeeper: ports 9092, 2181
- user-service: port 8081
- product-service: port 8082
- media-service: port 8083

### 2. Verify Services are Running
```powershell
docker-compose ps
curl http://localhost:8081/api/auth/register
curl http://localhost:8082/api/products
curl http://localhost:8083/api/media/upload
```

---

## AUDIT QUESTION 1: Initial Setup & Access

### Does the application run seamlessly?

**Test:**
```powershell
# Check user-service health
curl http://localhost:8081/api/auth/register

# Check product-service health
curl http://localhost:8082/api/products

# Check media-service health  
curl http://localhost:8083/api/media/upload
```

**Expected:** All services respond (even with errors for missing data - that's OK, it means they're running)

---

## AUDIT QUESTION 2: User & Product CRUD Operations

### A. User Registration (Client)

```powershell
curl -X POST http://localhost:8081/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "client@test.com",
    "password": "Password123!",
    "firstName": "John",
    "lastName": "Client",
    "phone": "1234567890",
    "role": "CLIENT"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGc...",
  "userId": "abc123",
  "email": "client@test.com",
  "role": "CLIENT"
}
```

### B. User Registration (Seller)

```powershell
curl -X POST http://localhost:8081/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "seller@test.com",
    "password": "Password123!",
    "firstName": "Jane",
    "lastName": "Seller",
    "phone": "0987654321",
    "role": "SELLER"
  }'
```

Save the token from response as `$SELLER_TOKEN`

### C. Login

```powershell
curl -X POST http://localhost:8081/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "seller@test.com",
    "password": "Password123!"
  }'
```

### D. Get User Profile

```powershell
$TOKEN = "your-jwt-token-here"

curl http://localhost:8081/api/auth/profile `
  -H "Authorization: Bearer $TOKEN"
```

### E. Update User Profile (with authorization check)

```powershell
# Get your user ID from profile response first
$USER_ID = "your-user-id"

curl -X PUT http://localhost:8081/api/users/$USER_ID `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{
    "firstName": "Jane Updated",
    "lastName": "Seller",
    "phone": "1111111111"
  }'
```

**✅ PASS:** Own profile updates successfully  
**✅ PASS:** Trying to update another user's profile returns 403 Forbidden

---

## AUDIT QUESTION 3: Authentication & Role Validation

### A. Create Product as Seller

```powershell
$SELLER_TOKEN = "seller-jwt-token"
$SELLER_EMAIL = "seller@test.com"
$SELLER_ID = "seller-user-id"

curl -X POST http://localhost:8082/api/products `
  -H "Authorization: Bearer $SELLER_TOKEN" `
  -H "X-User-Email: $SELLER_EMAIL" `
  -H "X-User-Id: $SELLER_ID" `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop with RTX 4070",
    "price": 1299.99,
    "stock": 50,
    "category": "Electronics"
  }'
```

**Expected:** Product created successfully (201 Created)

### B. Try to Create Product as Client (Should Fail)

```powershell
$CLIENT_TOKEN = "client-jwt-token"

curl -X POST http://localhost:8082/api/products `
  -H "Authorization: Bearer $CLIENT_TOKEN" `
  -H "X-User-Email: client@test.com" `
  -H "X-User-Id: client-id" `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Laptop",
    "description": "Test",
    "price": 100,
    "stock": 1,
    "category": "Test"
  }'
```

**Expected:** 403 Forbidden - "Only sellers can create products"

### C. Get All Products (Public Access)

```powershell
curl http://localhost:8082/api/products
```

**Expected:** List of all products (no auth required for viewing)

### D. Get Seller's Products

```powershell
curl http://localhost:8082/api/products/seller/seller@test.com
```

### E. Update Product (Only Owner Can Update)

```powershell
$PRODUCT_ID = "product-id-from-create-response"

# Seller updates own product (should work)
curl -X PUT http://localhost:8082/api/products/$PRODUCT_ID `
  -H "X-User-Email: seller@test.com" `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Gaming Laptop Updated",
    "description": "Updated description",
    "price": 1199.99,
    "stock": 45,
    "category": "Electronics"
  }'
```

**✅ PASS:** Owner can update  
**❌ FAIL:** Different seller trying to update returns 403

### F. Delete Product (Only Owner Can Delete)

```powershell
curl -X DELETE http://localhost:8082/api/products/$PRODUCT_ID `
  -H "X-User-Email: seller@test.com"
```

**✅ PASS:** Owner can delete  
**❌ FAIL:** Different user trying to delete returns 403

---

## AUDIT QUESTION 4: Media Upload & Product Association

### A. Upload Media for Product (Valid Image)

```powershell
# Create a test image first
$PRODUCT_ID = "your-product-id"

curl -X POST http://localhost:8083/api/media/upload `
  -H "X-User-Email: seller@test.com" `
  -F "file=@test-image.jpg" `
  -F "productId=$PRODUCT_ID"
```

**Expected Response:**
```json
{
  "id": "media-id",
  "filename": "uuid.jpg",
  "originalFilename": "test-image.jpg",
  "contentType": "image/jpeg",
  "fileSize": 524288,
  "url": "/api/media/files/uuid.jpg",
  "uploadedBy": "seller@test.com",
  "productId": "product-id",
  "uploadedAt": "2025-12-02T..."
}
```

### B. Test File Size Validation (Should Fail for >2MB)

```powershell
# Create a file larger than 2MB for testing
curl -X POST http://localhost:8083/api/media/upload `
  -H "X-User-Email: seller@test.com" `
  -F "file=@large-file.jpg" `
  -F "productId=$PRODUCT_ID"
```

**Expected:** 400 Bad Request - "File size exceeds 2MB limit"

### C. Test File Type Validation (Should Fail for Non-Images)

```powershell
curl -X POST http://localhost:8083/api/media/upload `
  -H "X-User-Email: seller@test.com" `
  -F "file=@document.pdf" `
  -F "productId=$PRODUCT_ID"
```

**Expected:** 400 Bad Request - "Only image files are allowed"

### D. Get Media by Product

```powershell
curl http://localhost:8083/api/media/product/$PRODUCT_ID
```

**Expected:** List of all media files for that product

### E. Download/View Media File

```powershell
curl http://localhost:8083/api/media/files/uuid.jpg --output downloaded.jpg
```

---

## AUDIT QUESTION 5: Security Measures

### A. Password Hashing

**Test:** Check database directly
```powershell
docker exec -it mongodb-user mongosh "mongodb://admin:admin123@localhost:27017/userdb?authSource=admin"

db.users.findOne()
```

**✅ PASS:** Password field shows bcrypt hash (starts with `$2a$` or `$2b$`)  
**❌ FAIL:** Plain text password visible

### B. Sensitive Data Protection

**Test:** Get user profile
```powershell
curl http://localhost:8081/api/auth/profile `
  -H "Authorization: Bearer $TOKEN"
```

**✅ PASS:** Response does NOT include password field  
**✅ PASS:** Only returns: id, email, firstName, lastName, phone, role, avatarUrl

### C. Input Validation

**Test 1: Invalid Email**
```powershell
curl -X POST http://localhost:8081/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "invalid-email",
    "password": "Password123!",
    "firstName": "Test",
    "lastName": "User",
    "phone": "1234567890",
    "role": "CLIENT"
  }'
```

**Expected:** 400 Bad Request with validation error

**Test 2: Short Password**
```powershell
curl -X POST http://localhost:8081/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "test@test.com",
    "password": "123",
    "firstName": "Test",
    "lastName": "User",
    "phone": "1234567890",
    "role": "CLIENT"
  }'
```

**Expected:** 400 Bad Request - password validation error

**Test 3: Missing Required Fields**
```powershell
curl -X POST http://localhost:8082/api/products `
  -H "X-User-Email: seller@test.com" `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Test"
  }'
```

**Expected:** 400 Bad Request with field-specific errors

### D. Authorization Checks

**Test 1: Access Protected Endpoint Without Token**
```powershell
curl http://localhost:8081/api/auth/profile
```

**Expected:** 401 Unauthorized or 403 Forbidden

**Test 2: Update Another User's Profile**
```powershell
curl -X PUT http://localhost:8081/api/users/different-user-id `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{"firstName":"Hacker"}'
```

**Expected:** 403 Forbidden - "You can only update your own profile"

---

## AUDIT QUESTION 6: Error Handling & Edge Cases

### A. Register with Existing Email

```powershell
# Register first time
curl -X POST http://localhost:8081/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "duplicate@test.com",
    "password": "Password123!",
    "firstName": "Test",
    "lastName": "User",
    "phone": "1234567890",
    "role": "CLIENT"
  }'

# Try to register again with same email
curl -X POST http://localhost:8081/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "duplicate@test.com",
    "password": "Password123!",
    "firstName": "Another",
    "lastName": "User",
    "phone": "9999999999",
    "role": "SELLER"
  }'
```

**Expected:** 409 Conflict - "User with email duplicate@test.com already exists"

### B. Upload Invalid File Format

```powershell
curl -X POST http://localhost:8083/api/media/upload `
  -H "X-User-Email: seller@test.com" `
  -F "file=@script.exe"
```

**Expected:** 400 Bad Request - "Only image files are allowed"

### C. Upload File Exceeding Size Limit

**Expected:** 400 Bad Request - "File size exceeds 2MB limit"

### D. Access Non-Existent Resource

```powershell
curl http://localhost:8082/api/products/non-existent-id
```

**Expected:** 404 Not Found - "Product not found with id: non-existent-id"

---

## Summary Checklist

| Audit Question | Test | Status |
|----------------|------|--------|
| **1. Initial Setup** | Services run via docker-compose | ✅ |
| **2. User CRUD** | Register, login, profile, update | ✅ |
| **2. Product CRUD** | Create, read, update, delete | ✅ |
| **3. Role Validation** | Seller can create products, client cannot | ✅ |
| **3. Ownership** | Only owner can update/delete product | ✅ |
| **4. Media Upload** | Image upload works | ✅ |
| **4. Size Constraint** | 2MB limit enforced | ✅ |
| **4. Type Constraint** | Only images allowed | ✅ |
| **4. Product Association** | Media linked to products | ✅ |
| **5. Password Hashing** | Bcrypt used | ✅ |
| **5. Sensitive Data** | Password not in responses | ✅ |
| **5. Authorization** | Users can only modify own resources | ✅ |
| **6. Duplicate Email** | Handled gracefully | ✅ |
| **6. Invalid File Type** | Proper error message | ✅ |
| **6. File Size Exceeded** | Proper error message | ✅ |
| **6. Resource Not Found** | 404 with clear message | ✅ |

---

## Next Steps

1. **Frontend Implementation** - Angular app needed for web UI
2. **API Gateway** - Centralized entry point and JWT validation
3. **HTTPS Setup** - SSL/TLS certificates for production
4. **Integration Tests** - Automated test suite
5. **CI/CD Pipeline** - Automated deployment

---

## Notes

- All backend services are Java 21 with Spring Boot 3.2.0
- MongoDB used for all persistence
- Kafka ready for event-driven communication
- Passwords hashed with bcrypt
- File upload validated with Apache Tika (not just content-type header)
- Role-based access control enforced at service level
- Comprehensive error handling with meaningful messages
