# E-Commerce Microservices Platform

A comprehensive e-commerce platform built with **Java 21**, Spring Boot microservices, MongoDB, Kafka, and Angular.

## 🎯 Project Status

✅ **Backend Services Completed** (Java 21 + Spring Boot 3.2.0)  
✅ **Docker Integration** (docker-compose with all services)  
✅ **API Testing Guide** (comprehensive audit checklist)  
📋 **Frontend Guide** (complete Angular implementation guide provided)  

See `TODO.txt` for detailed status.

## 🏗️ Architecture Overview

### Microservices (All Java 21)
1. **User Service** (Port 8081) - Authentication, registration, profile management, avatar upload
2. **Product Service** (Port 8082) - Product CRUD with seller-only authorization
3. **Media Service** (Port 8083) - Image upload with 2MB limit and type validation
4. **API Gateway** (Port 8080) - Optional centralized routing _(not yet implemented)_

### Technologies
- **Backend**: Java 21, Spring Boot 3.2.0, MongoDB, Kafka, JWT, Apache Tika
- **Frontend**: Angular 17+ _(implementation guide provided)_
- **Infrastructure**: Docker, Docker Compose
- **Security**: BCrypt, JWT, Role-based access control

## ✨ Features

### User Management (user-service)
- ✅ User registration as CLIENT or SELLER
- ✅ JWT-based authentication
- ✅ Profile management (get/update with authorization)
- ✅ Avatar upload (2MB limit, image validation)
- ✅ Password hashing with BCrypt
- ✅ User can only update own profile (JWT validation)

### Product Management (product-service)
- ✅ CRUD operations (sellers only)
- ✅ Seller ownership validation (only owner can update/delete)
- ✅ Product-image associations
- ✅ Query by seller, category, or all products
- ✅ MongoDB persistence
- ✅ Kafka event configuration

### Media Management (media-service)
- ✅ Image upload with strict validation
- ✅ 2MB file size limit enforced
- ✅ File type detection using Apache Tika (not just content-type)
- ✅ Only images allowed (JPEG, PNG, GIF, WebP)
- ✅ MongoDB metadata storage
- ✅ File serving endpoint
- ✅ Product association

### Security & Validation
- ✅ BCrypt password hashing
- ✅ JWT token authentication
- ✅ Role-based authorization (SELLER vs CLIENT)
- ✅ Ownership validation (users can only modify their own resources)
- ✅ Sensitive data protection (passwords never in API responses)
- ✅ Comprehensive input validation (Bean Validation)
- ✅ Global exception handling with meaningful error messages
- ✅ File upload constraints enforced

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local development)
- Maven (for local development)
- Node.js 18+ & Angular CLI (for frontend)
- Java 17+ (for local development)
- Node.js 18+ & npm (for frontend development)

### Running with Docker

```bash
# Clone and start
git clone https://github.com/cmbigk/buy-01.git
cd buy-01
docker-compose up --build
```

### Access Points
- Frontend: http://localhost:4200
- API Gateway: http://localhost:8080
- User Service: http://localhost:8081
- Product Service: http://localhost:8082
- Media Service: http://localhost:8083

## API Endpoints

### User Service
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/auth/profile` - Get current user profile (auth required)
- `PUT /api/auth/profile` - Update profile (auth required)
- `POST /api/auth/avatar` - Upload avatar (seller only)

### Product Service
- `GET /api/products` - Get all products
- `POST /api/products` - Create product (seller only)
- `PUT /api/products/{id}` - Update product (owner only)
- `DELETE /api/products/{id}` - Delete product (owner only)

### Media Service
- `POST /api/media/upload` - Upload media (seller only)
- `GET /api/media/{id}` - Get media by ID
- `DELETE /api/media/{id}` - Delete media (owner only)

## Testing with Postman

1. **Register as Seller**
```json
POST /api/auth/register
{
  "email": "seller@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Seller",
  "role": "SELLER"
}
```

2. **Login** to get JWT token

3. **Create Product** with Bearer token

4. **Upload Image** with product ID

## License

MIT License