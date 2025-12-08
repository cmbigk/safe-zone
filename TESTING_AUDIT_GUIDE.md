# Comprehensive Testing & Audit Guide

This guide covers all testing scenarios to verify the e-commerce platform meets all requirements and audit criteria.

---

## 🚀 Initial Setup and Launch

### 1. Clone and Start Application

```bash
# Clone repository
git clone <repository-url>
cd buy-01

# Start all services with Docker
docker-compose up --build -d

# Verify all containers are running
docker-compose ps
```

**Expected Output:**
```
mongodb-user      Up    0.0.0.0:27017->27017/tcp
mongodb-product   Up    0.0.0.0:27018->27017/tcp
mongodb-media     Up    0.0.0.0:27019->27017/tcp
user-service      Up    0.0.0.0:8081->8081/tcp
product-service   Up    0.0.0.0:8082->8082/tcp
media-service     Up    0.0.0.0:8083->8083/tcp
zookeeper         Up    0.0.0.0:2181->2181/tcp
```

### 2. Access Application

- **Frontend:** http://localhost:4200
- **User API:** http://localhost:8081
- **Product API:** http://localhost:8082
- **Media API:** http://localhost:8083

---

## 👤 User CRUD Operations Testing

### Test 1: Register as Client

**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "email": "client@test.com",
  "password": "Client123!",
  "firstName": "John",
  "lastName": "Client",
  "phone": "+1234567890",
  "role": "CLIENT"
}
```

**Expected Response (201):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "675636c3862adf4b36f77a1b",
  "email": "client@test.com",
  "role": "CLIENT"
}
```

**Verify:**
- ✅ User created successfully
- ✅ JWT token returned
- ✅ Password is hashed (check MongoDB)
- ✅ Role is CLIENT

### Test 2: Register as Seller

**Request:**
```json
{
  "email": "seller@test.com",
  "password": "Seller123!",
  "firstName": "Jane",
  "lastName": "Seller",
  "phone": "+1987654321",
  "role": "SELLER"
}
```

**Expected Response (201):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "675636c3862adf4b36f77a1c",
  "email": "seller@test.com",
  "role": "SELLER"
}
```

**Verify:**
- ✅ Seller created successfully
- ✅ JWT token returned
- ✅ Role is SELLER

### Test 3: Login as Client

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "email": "client@test.com",
  "password": "Client123!"
}
```

**Expected Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "675636c3862adf4b36f77a1b",
  "email": "client@test.com",
  "role": "CLIENT"
}
```

### Test 4: Get User Profile

**Endpoint:** `GET /api/auth/profile`

**Headers:**
```
Authorization: Bearer <token>
```

**Expected Response (200):**
```json
{
  "id": "675636c3862adf4b36f77a1b",
  "email": "client@test.com",
  "firstName": "John",
  "lastName": "Client",
  "phone": "+1234567890",
  "role": "CLIENT",
  "avatarUrl": null,
  "createdAt": "2025-12-09T10:30:00",
  "updatedAt": "2025-12-09T10:30:00"
}
```

**Verify:**
- ✅ Password NOT included in response
- ✅ All user details present
- ✅ Requires authentication

### Test 5: Update Profile

**Endpoint:** `PUT /api/auth/profile`

**Headers:**
```
Authorization: Bearer <seller_token>
```

**Request:**
```json
{
  "firstName": "Jane",
  "lastName": "Seller Updated",
  "phone": "+1987654999"
}
```

**Expected Response (200):**
```json
{
  "id": "675636c3862adf4b36f77a1c",
  "email": "seller@test.com",
  "firstName": "Jane",
  "lastName": "Seller Updated",
  "phone": "+1987654999",
  "role": "SELLER",
  "avatarUrl": null,
  "createdAt": "2025-12-09T10:30:00",
  "updatedAt": "2025-12-09T10:35:00"
}
```

### Test 6: Upload Seller Avatar

**Endpoint:** `POST /api/auth/avatar`

**Headers:**
```
Authorization: Bearer <seller_token>
Content-Type: multipart/form-data
```

**Body:** (Form Data)
- `file`: [Select image file < 2MB]

**Expected Response (200):**
```json
{
  "message": "Avatar uploaded successfully",
  "avatarUrl": "/uploads/avatars/550e8400-e29b-41d4-a716-446655440000.jpg"
}
```

**Verify:**
- ✅ Only image files accepted
- ✅ File size limit enforced (2MB)
- ✅ File stored with UUID filename
- ✅ Avatar URL returned

---

## 📦 Product CRUD Operations Testing

### Test 7: Create Product (as Seller)

**Endpoint:** `POST /api/products`

**Headers:**
```
Authorization: Bearer <seller_token>
X-User-Email: seller@test.com
X-User-Id: 675636c3862adf4b36f77a1c
```

**Request:**
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 Pro chip and titanium design",
  "price": 999.99,
  "category": "Electronics",
  "stock": 50,
  "imageIds": []
}
```

**Expected Response (201):**
```json
{
  "id": "69374396b4fff718ae7bd432",
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 Pro chip and titanium design",
  "price": 999.99,
  "stock": 50,
  "category": "Electronics",
  "sellerId": "675636c3862adf4b36f77a1c",
  "sellerEmail": "seller@test.com",
  "imageIds": [],
  "createdAt": "2025-12-09T11:00:00",
  "updatedAt": "2025-12-09T11:00:00"
}
```

**Verify:**
- ✅ Only sellers can create products
- ✅ Product created successfully
- ✅ Seller information attached
- ✅ Can create without images

### Test 8: Get All Products

**Endpoint:** `GET /api/products`

**Expected Response (200):**
```json
[
  {
    "id": "69374396b4fff718ae7bd432",
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone...",
    "price": 999.99,
    "stock": 50,
    "category": "Electronics",
    "sellerId": "675636c3862adf4b36f77a1c",
    "sellerEmail": "seller@test.com",
    "imageIds": [],
    "createdAt": "2025-12-09T11:00:00",
    "updatedAt": "2025-12-09T11:00:00"
  }
]
```

**Verify:**
- ✅ No authentication required
- ✅ All products visible to everyone
- ✅ Seller information included

### Test 9: Get Products by Seller

**Endpoint:** `GET /api/products/seller/seller@test.com`

**Expected Response (200):**
```json
[
  {
    "id": "69374396b4fff718ae7bd432",
    "name": "iPhone 15 Pro",
    ...
  }
]
```

**Verify:**
- ✅ Returns only products by specified seller
- ✅ No authentication required for viewing

### Test 10: Update Product (as Owner)

**Endpoint:** `PUT /api/products/69374396b4fff718ae7bd432`

**Headers:**
```
Authorization: Bearer <seller_token>
X-User-Email: seller@test.com
```

**Request:**
```json
{
  "name": "iPhone 15 Pro Max",
  "description": "Updated description",
  "price": 1099.99,
  "category": "Electronics",
  "stock": 45,
  "imageIds": []
}
```

**Expected Response (200):**
```json
{
  "id": "69374396b4fff718ae7bd432",
  "name": "iPhone 15 Pro Max",
  "price": 1099.99,
  ...
}
```

**Verify:**
- ✅ Only product owner can update
- ✅ Updates successful
- ✅ Timestamp updated

### Test 11: Delete Product (as Owner)

**Endpoint:** `DELETE /api/products/69374396b4fff718ae7bd432`

**Headers:**
```
Authorization: Bearer <seller_token>
X-User-Email: seller@test.com
```

**Expected Response (204 No Content)**

**Verify:**
- ✅ Only product owner can delete
- ✅ Product removed from database
- ✅ No content returned

---

## 🖼️ Media Upload & Management Testing

### Test 12: Upload Media for Product

**Endpoint:** `POST /api/media/upload`

**Headers:**
```
X-User-Email: seller@test.com
Content-Type: multipart/form-data
```

**Body:** (Form Data)
- `file`: [Image file]
- `productId`: "69374396b4fff718ae7bd432"

**Expected Response (201):**
```json
{
  "id": "67564a12b4fff718ae7bd435",
  "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "contentType": "image/jpeg",
  "fileSize": 1048576,
  "uploadedBy": "seller@test.com",
  "productId": "69374396b4fff718ae7bd432",
  "uploadedAt": "2025-12-09T11:15:00"
}
```

**Verify:**
- ✅ Image uploaded successfully
- ✅ Associated with product
- ✅ UUID filename generated
- ✅ Metadata stored in database

### Test 13: Get Media by Product

**Endpoint:** `GET /api/media/product/69374396b4fff718ae7bd432`

**Expected Response (200):**
```json
[
  {
    "id": "67564a12b4fff718ae7bd435",
    "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
    ...
  }
]
```

### Test 14: Get Media File

**Endpoint:** `GET /api/media/files/550e8400-e29b-41d4-a716-446655440000.jpg`

**Expected:** Image file downloaded

**Verify:**
- ✅ File served correctly
- ✅ Correct content type
- ✅ File accessible

---

## 🔒 Authentication & Authorization Testing

### Test 15: Client Cannot Create Product

**Endpoint:** `POST /api/products`

**Headers:**
```
Authorization: Bearer <client_token>
X-User-Email: client@test.com
X-User-Id: 675636c3862adf4b36f77a1b
```

**Request:** (Same as Test 7)

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "Only sellers can create products",
  "timestamp": "2025-12-09T11:20:00"
}
```

**Verify:**
- ✅ Clients blocked from creating products
- ✅ Proper error message
- ✅ HTTP 403 status

### Test 16: Seller Cannot Modify Another Seller's Product

**Endpoint:** `PUT /api/products/{another_seller_product_id}`

**Headers:**
```
X-User-Email: seller@test.com
```

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "You can only update your own products",
  "timestamp": "2025-12-09T11:25:00"
}
```

**Verify:**
- ✅ Sellers can only modify own products
- ✅ Authorization enforced
- ✅ Proper error message

### Test 17: Access Protected Endpoint Without Token

**Endpoint:** `GET /api/auth/profile`

**Headers:** (No Authorization header)

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "Access Denied",
  "timestamp": "2025-12-09T11:30:00"
}
```

**Verify:**
- ✅ Authentication required
- ✅ Unauthenticated requests blocked
- ✅ Proper error response

---

## ⚠️ Error Handling & Edge Cases

### Test 18: Register with Existing Email

**Request:**
```json
{
  "email": "seller@test.com",
  "password": "AnotherPass123!",
  "firstName": "Duplicate",
  "lastName": "User",
  "phone": "+1111111111",
  "role": "SELLER"
}
```

**Expected Response (409 Conflict):**
```json
{
  "status": 409,
  "message": "User with email seller@test.com already exists",
  "timestamp": "2025-12-09T11:35:00"
}
```

**Verify:**
- ✅ Duplicate email detected
- ✅ HTTP 409 status
- ✅ Clear error message

### Test 19: Login with Wrong Password

**Request:**
```json
{
  "email": "seller@test.com",
  "password": "WrongPassword123!"
}
```

**Expected Response (401 Unauthorized):**
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "timestamp": "2025-12-09T11:40:00"
}
```

**Verify:**
- ✅ Invalid credentials rejected
- ✅ Generic error message (security)
- ✅ HTTP 401 status

### Test 20: Register with Invalid Email

**Request:**
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
  "timestamp": "2025-12-09T11:45:00"
}
```

**Verify:**
- ✅ Email format validated
- ✅ Clear validation message
- ✅ HTTP 400 status

### Test 21: Register with Short Password

**Request:**
```json
{
  "email": "test@test.com",
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
  "timestamp": "2025-12-09T11:50:00"
}
```

**Verify:**
- ✅ Password length enforced
- ✅ Clear error message
- ✅ HTTP 400 status

### Test 22: Upload Oversized Media (> 2MB)

**Endpoint:** `POST /api/media/upload`

**Body:** File larger than 2MB

**Expected Response (413 Payload Too Large):**
```json
{
  "status": 413,
  "message": "File size exceeds maximum limit of 2MB",
  "timestamp": "2025-12-09T11:55:00"
}
```

**Verify:**
- ✅ File size limit enforced
- ✅ HTTP 413 status
- ✅ Clear error message

### Test 23: Upload Invalid File Type

**Endpoint:** `POST /api/media/upload`

**Body:** Non-image file (e.g., .pdf, .txt)

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Only image files (jpg, jpeg, png, gif) are allowed",
  "timestamp": "2025-12-09T12:00:00"
}
```

**Verify:**
- ✅ File type validated
- ✅ Only images accepted
- ✅ Clear error message

### Test 24: Create Product with Missing Fields

**Request:**
```json
{
  "name": "",
  "description": "Short",
  "price": -10,
  "category": "",
  "stock": -5
}
```

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Validation failed: {name=Product name is required, description=Description must be between 10 and 1000 characters, price=Price must be greater than 0, stock=Stock cannot be negative, category=Category is required}",
  "timestamp": "2025-12-09T12:05:00"
}
```

**Verify:**
- ✅ All validations enforced
- ✅ Multiple errors reported
- ✅ Clear field-level errors

---

## 🔐 Security Verification

### Test 25: Password Hashing

**Steps:**
1. Register a user
2. Check MongoDB for user document
3. Verify password field

**MongoDB Query:**
```javascript
db.user.findOne({ "email": "seller@test.com" })
```

**Expected:**
```json
{
  "_id": ObjectId("..."),
  "email": "seller@test.com",
  "password": "$2a$10$Xt5H3...", // BCrypt hash
  "firstName": "Jane",
  ...
}
```

**Verify:**
- ✅ Password is hashed with BCrypt
- ✅ Original password not stored
- ✅ Password never returned in API responses

### Test 26: JWT Token Validation

**Steps:**
1. Login and get token
2. Decode token at jwt.io
3. Verify claims

**Token Payload:**
```json
{
  "sub": "seller@test.com",
  "iat": 1733745600,
  "exp": 1733832000
}
```

**Verify:**
- ✅ Subject contains user email
- ✅ Token has expiration (24 hours)
- ✅ Token signed with secret
- ✅ Token required for protected endpoints

### Test 27: HTTPS Enforcement

**Manual Test:**
1. Access http://localhost
2. Should redirect to https://localhost

**cURL Test:**
```bash
curl -I http://localhost
```

**Expected:**
```
HTTP/1.1 301 Moved Permanently
Location: https://localhost
```

**Verify:**
- ✅ HTTP redirects to HTTPS
- ✅ HSTS header present
- ✅ Secure cookies used

### Test 28: Security Headers

**Request:**
```bash
curl -I https://localhost
```

**Expected Headers:**
```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
```

**Verify:**
- ✅ All security headers present
- ✅ HSTS enabled
- ✅ XSS protection active
- ✅ Clickjacking protection

---

## 🎨 Frontend Testing

### Test 29: Registration Page

**URL:** http://localhost:4200/register

**Test Cases:**
1. Register as Client
   - ✅ Form validation works
   - ✅ Password field masked
   - ✅ Role selection available
   - ✅ Success message shown
   - ✅ Redirects after registration

2. Register as Seller
   - ✅ Same validations
   - ✅ Avatar upload option visible
   - ✅ Form submits correctly

3. Validation Errors
   - ✅ Invalid email shows error
   - ✅ Short password shows error
   - ✅ Required fields highlighted

### Test 30: Login Page

**URL:** http://localhost:4200/login

**Test Cases:**
1. Valid Login
   - ✅ Accepts credentials
   - ✅ Shows loading state
   - ✅ Stores token
   - ✅ Redirects to appropriate page

2. Invalid Login
   - ✅ Shows error message
   - ✅ Doesn't redirect
   - ✅ Clears password field

### Test 31: Product Listing Page

**URL:** http://localhost:4200/products

**Test Cases:**
1. View All Products
   - ✅ Products displayed in grid
   - ✅ Product images shown
   - ✅ Price and stock visible
   - ✅ Seller information displayed
   - ✅ Products without images handled gracefully

2. Empty State
   - ✅ Shows "No products" message
   - ✅ Provides action button for sellers

### Test 32: Seller Dashboard

**URL:** http://localhost:4200/dashboard

**Test Cases:**
1. Access Control
   - ✅ Only sellers can access
   - ✅ Clients redirected to products page

2. Product Management
   - ✅ Create new product form opens
   - ✅ All fields validated
   - ✅ Image upload works
   - ✅ Product list shows seller's products
   - ✅ Edit button pre-fills form
   - ✅ Delete confirmation shown

3. Media Upload
   - ✅ File selection works
   - ✅ Upload button enabled/disabled correctly
   - ✅ Progress indication
   - ✅ Success/error messages
   - ✅ File size validation
   - ✅ File type validation

---

## 📊 Database Verification

### Test 33: MongoDB Collections

**Check User Collection:**
```javascript
// Connect to MongoDB
mongo mongodb://admin:admin123@localhost:27017/userdb?authSource=admin

// Query users
db.user.find().pretty()
```

**Verify:**
- ✅ Passwords are hashed
- ✅ Email is indexed and unique
- ✅ Roles are correct
- ✅ Timestamps present

**Check Product Collection:**
```javascript
mongo mongodb://admin:admin123@localhost:27018/productdb?authSource=admin
db.product.find().pretty()
```

**Verify:**
- ✅ Seller information included
- ✅ Image IDs array present
- ✅ Prices stored as decimal
- ✅ Timestamps present

**Check Media Collection:**
```javascript
mongo mongodb://admin:admin123@localhost:27019/mediadb?authSource=admin
db.media.find().pretty()
```

**Verify:**
- ✅ Uploader email stored
- ✅ Product associations correct
- ✅ File metadata accurate
- ✅ Timestamps present

---

## 🧪 Integration Testing

### Test 34: Complete User Flow

1. **Register as Seller**
   - Create account
   - Verify token received
   - Upload avatar

2. **Create Product**
   - Upload product image
   - Create product with image
   - Verify product appears in dashboard

3. **View as Client**
   - Register as client
   - View product listing
   - Verify product visible with image

4. **Manage Product**
   - Login as seller
   - Edit product
   - Delete product
   - Verify changes

**Verify:**
- ✅ Complete flow works end-to-end
- ✅ No errors encountered
- ✅ Data persists correctly

---

## 📈 Performance Testing

### Test 35: Load Testing (Optional)

**Using Apache Bench:**
```bash
# Test product listing endpoint
ab -n 1000 -c 10 http://localhost:8082/api/products

# Test authentication endpoint
ab -n 100 -c 5 -p register.json -T application/json http://localhost:8081/api/auth/register
```

**Verify:**
- ✅ Acceptable response times
- ✅ No failures under load
- ✅ Database handles concurrent requests

---

## ✅ Audit Checklist

### Application Functionality
- [x] Docker setup works correctly
- [x] All services start successfully
- [x] Frontend accessible in browser
- [x] API endpoints respond correctly

### User CRUD Operations
- [x] User registration (client & seller)
- [x] User login and authentication
- [x] Profile retrieval
- [x] Profile updates
- [x] Avatar upload for sellers

### Product CRUD Operations
- [x] Product creation (sellers only)
- [x] Product listing (public)
- [x] Product updates (owner only)
- [x] Product deletion (owner only)
- [x] Product filtering by seller

### Authentication & Authorization
- [x] Role-based access control
- [x] JWT token generation
- [x] Token validation
- [x] Seller vs Client restrictions
- [x] Ownership verification

### Media Management
- [x] Image upload
- [x] File size validation (2MB)
- [x] File type validation
- [x] Product association
- [x] File retrieval

### Security Measures
- [x] Password hashing (BCrypt)
- [x] HTTPS encryption
- [x] Security headers
- [x] Input validation
- [x] Sensitive data protection
- [x] Access control enforcement

### Error Handling
- [x] Validation errors
- [x] Authentication errors
- [x] Authorization errors
- [x] Duplicate email handling
- [x] Invalid credentials
- [x] File size/type errors
- [x] Missing required fields

### Code Quality
- [x] Spring annotations used correctly
- [x] Proper service layer separation
- [x] Repository pattern implemented
- [x] DTOs used appropriately
- [x] Exception handling centralized

### Frontend Implementation
- [x] Angular components structured
- [x] Services implemented
- [x] Routing configured
- [x] Forms with validation
- [x] HTTP interceptors
- [x] Error handling

---

## 📝 Test Report Template

```
Test Date: ___________
Tester: ___________

| Test # | Test Name | Status | Notes |
|--------|-----------|---------|-------|
| 1 | Register as Client | ✅ PASS | |
| 2 | Register as Seller | ✅ PASS | |
| ... | ... | ... | ... |

Issues Found:
1. [Description]
2. [Description]

Overall Assessment: PASS / FAIL
```

---

**All tests should pass for production readiness!**
