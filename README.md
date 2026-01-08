# E-Commerce Microservices Platform with CI/CD

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-red)]()
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue)]()

A comprehensive e-commerce platform built with **Java 21**, Spring Boot microservices, MongoDB, Kafka, and Angular. **Now with complete Jenkins CI/CD pipeline!**

## 🎯 Project Status

✅ **Backend Services Completed** (Java 21 + Spring Boot 3.2.0)  
✅ **Docker Integration** (docker-compose with all services)  
✅ **API Testing Guide** (comprehensive audit checklist)  
✅ **Jenkins CI/CD Pipeline** (automated testing, deployment, rollback)  
✅ **GitHub Integration** (auto-trigger builds on commit)  
📋 **Frontend Guide** (complete Angular implementation guide provided)  

## 🚀 CI/CD Pipeline

This project includes a complete Jenkins CI/CD setup with:
- ✅ Automated testing (JUnit + Jasmine/Karma)
- ✅ Blue-green deployment with rollback
- ✅ Email & Slack notifications
- ✅ GitHub webhook integration
- ✅ Parameterized builds (dev/staging/production)

**Quick Start CI/CD:**
```bash
cd deployment
./start-jenkins.sh
```
Then open http://localhost:8090

**📚 Complete CI/CD Documentation:** See [deployment/README.md](deployment/README.md)

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

**Required for Docker (Recommended):**
- Docker Desktop (latest version)
- Docker Compose (included with Docker Desktop)

**Required for Local Development:**
- Java 21 (OpenJDK or Eclipse Temurin)
- Maven 3.9+ 
- Node.js 18+ LTS
- Angular CLI (`npm install -g @angular/cli`)

**Verify Installations:**
```powershell
# Check Docker
docker --version          # Docker version 24.0+
docker-compose --version  # Docker Compose version 2.0+

# Check Java & Maven
java -version             # openjdk version "21.x.x"
mvn -version              # Apache Maven 3.9+

# Check Node & Angular
node --version            # v18.x or v20.x
npm --version             # 9.x or 10.x
ng version                # Angular CLI 17.x or 18.x
```

---

## 🏃 Running the Application

### **Option 1: Docker Compose (Recommended for Full Stack)**

Complete microservices stack with MongoDB, Kafka, and all services:

```powershell
# 1. Clone the repository
git clone https://github.com/cmbigk/buy-01.git
cd buy-01

# 2. Start all services (backend + databases)
docker-compose up --build -d

# 3. Verify all containers are running
docker ps

# Expected output: 7 containers running
# - user-service (port 8081)
# - product-service (port 8082)
# - media-service (port 8083)
# - mongodb-user (port 27017)
# - mongodb-product (port 27018)
# - mongodb-media (port 27019)
# - zookeeper (port 2181)

# 4. Check service health
docker logs user-service --tail 20      # Should show "Started UserServiceApplication"
docker logs product-service --tail 20   # Should show "Started ProductServiceApplication"
docker logs media-service --tail 20     # Should show "Started MediaServiceApplication"

# 5. Start Angular frontend (separate terminal)
cd frontend
npm install                # Only needed first time
ng serve

# 6. Access the application
# Frontend: http://localhost:4200
# User API: http://localhost:8081
# Product API: http://localhost:8082
# Media API: http://localhost:8083
```

**Docker Management Commands:**
```powershell
# Stop all services
docker-compose down

# Stop and remove all data (clean slate)
docker-compose down -v

# Restart a single service
docker-compose restart user-service

# View logs
docker-compose logs -f              # All services
docker-compose logs -f user-service # Specific service

# Rebuild after code changes
docker-compose up --build -d
```

---

### **Option 2: Local Development (Maven + Angular)**

Run services individually without Docker (requires manual MongoDB setup):

#### **Backend Services (Each in Separate Terminal)**

**Terminal 1 - User Service:**
```powershell
cd user-service

# Build the project
mvn clean install -DskipTests

# Run the service
mvn spring-boot:run

# OR run the JAR directly
java -jar target/user-service-0.0.1-SNAPSHOT.jar

# Verify: http://localhost:8081/actuator/health
```

**Terminal 2 - Product Service:**
```powershell
cd product-service

# Build and run
mvn clean install -DskipTests
mvn spring-boot:run

# Verify: http://localhost:8082/actuator/health
```

**Terminal 3 - Media Service:**
```powershell
cd media-service

# Build and run
mvn clean install -DskipTests
mvn spring-boot:run

# Verify: http://localhost:8083/actuator/health
```

#### **Frontend (Angular) - Terminal 4:**
```powershell
cd frontend

# Install dependencies (first time only)
npm install

# Start development server with proxy
ng serve

# Frontend available at: http://localhost:4200
# Proxy routes /api/* to backend services
```

**Local Development Notes:**
- Update `application.properties` in each service to point to your local MongoDB
- Default MongoDB connection: `mongodb://localhost:27017/{dbname}`
- Install MongoDB locally or use MongoDB Atlas (cloud)
- Kafka is optional for local development

---

### **Option 3: Hybrid (Docker Backend + Local Frontend)**

Run backend in Docker, frontend locally for faster Angular development:

```powershell
# 1. Start backend services only
docker-compose up -d user-service product-service media-service mongodb-user mongodb-product mongodb-media

# 2. Start Angular with live reload
cd frontend
npm install
ng serve

# 3. Make frontend changes - auto-reloads instantly
# Backend changes require: docker-compose restart <service-name>
```

---

## 🔍 Service Access Points

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:4200 | Angular UI (login, register, products, dashboard) |
| **User Service** | http://localhost:8081 | Authentication, profile management |
| **Product Service** | http://localhost:8082 | Product CRUD operations |
| **Media Service** | http://localhost:8083 | Image upload & serving |
| **MongoDB User** | mongodb://localhost:27017 | User database |
| **MongoDB Product** | mongodb://localhost:27018 | Product database |
| **MongoDB Media** | mongodb://localhost:27019 | Media metadata database |

---

## 🧪 Testing the Application

### **1. Quick Health Check**
```powershell
# Test if services are responding
Invoke-RestMethod -Uri "http://localhost:8081/actuator/health"  # User Service
Invoke-RestMethod -Uri "http://localhost:8082/actuator/health"  # Product Service
Invoke-RestMethod -Uri "http://localhost:4200"                  # Frontend
```

### **2. Manual Testing (Browser)**
1. Open http://localhost:4200
2. Click **Register** → Fill in details → Submit
3. Login with registered credentials
4. Navigate to **Dashboard** (sellers only)
5. Create a product with image upload
6. View products in **Products** page

### **3. API Testing (Postman)**
See `POSTMAN_API_TESTING_GUIDE.md` for comprehensive API test cases:
- User registration & authentication (18 test cases)
- Product CRUD operations (12 test cases)
- Media upload & validation
- Error handling & authorization

---

## 🛠️ Development Workflow

### **Making Backend Changes**

```powershell
# Option A: With Docker
cd user-service
# Make code changes...
cd ..
docker-compose up --build -d user-service  # Rebuilds only user-service

# Option B: Without Docker
cd user-service
# Make code changes...
mvn clean install -DskipTests
mvn spring-boot:run  # Restart Maven process
```

### **Making Frontend Changes**

```powershell
cd frontend
# Make code changes in src/...
# Angular auto-reloads (no restart needed with ng serve)

# Build for production
ng build --configuration production
# Output: frontend/dist/frontend/browser/
```

### **Database Management**

```powershell
# Connect to MongoDB containers
docker exec -it mongodb-user mongosh
docker exec -it mongodb-product mongosh
docker exec -it mongodb-media mongosh

# Inside mongosh:
show dbs                    # List databases
use userdb                  # Switch to userdb
db.users.find().pretty()    # View users
db.users.deleteMany({})     # Clear all users (testing)
```

---

## 📦 Build for Production

### **Backend (Create Executable JARs)**
```powershell
# Build all services
mvn clean package -DskipTests

# JARs created at:
# user-service/target/user-service-0.0.1-SNAPSHOT.jar
# product-service/target/product-service-0.0.1-SNAPSHOT.jar
# media-service/target/media-service-0.0.1-SNAPSHOT.jar

# Run production JAR
java -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar
```

### **Frontend (Production Build)**
```powershell
cd frontend
ng build --configuration production

# Output: frontend/dist/frontend/browser/
# Deploy these files to nginx, Apache, or CDN
```

### **Docker Production Images**
```powershell
# Build production images
docker-compose build

# Tag and push to registry
docker tag buy-01-user-service:latest yourusername/user-service:1.0
docker push yourusername/user-service:1.0
```

---

## 🐛 Troubleshooting

### **Port Already in Use**
```powershell
# Find process using port
netstat -ano | findstr :8081

# Kill process by PID
taskkill /PID <PID> /F

# Or change port in application.properties:
# server.port=8090
```

### **Docker Containers Not Starting**
```powershell
# Check logs
docker-compose logs user-service

# Remove all containers and volumes (clean slate)
docker-compose down -v
docker-compose up --build -d
```

### **Angular Build Errors**
```powershell
# Clear cache and reinstall
cd frontend
rm -r -fo node_modules
rm package-lock.json
npm install
ng serve
```

### **MongoDB Connection Failed**
```powershell
# Check if MongoDB is running
docker ps | Select-String mongodb

# Restart MongoDB
docker-compose restart mongodb-user

# Check connection string in application.properties:
# spring.data.mongodb.uri=mongodb://mongodb-user:27017/userdb
```

### **Backend Service Crashes**
```powershell
# Check Java version
java -version  # Must be 21.x

# Check logs for errors
docker logs user-service --tail 50

# Common issue: MongoDB not ready
# Wait 30 seconds and restart: docker-compose restart user-service
```

---

## 🎯 Next Steps

- ✅ **Backend & Frontend Setup**: Complete
- ✅ **API Testing**: See `POSTMAN_API_TESTING_GUIDE.md`
- 📋 **Remaining Work**: See `TODO.txt` for gateway and additional features

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

MIT License# CI/CD Pipeline Status: ✅ Build #11 Successful - Thu Jan  8 20:32:18 EET 2026
