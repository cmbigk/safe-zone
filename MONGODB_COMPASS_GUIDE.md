# MongoDB Compass Connection Guide

This guide shows you how to connect to all three MongoDB databases running in Docker containers using MongoDB Compass on your Mac.

---

## 📊 Database Overview

The application uses **3 separate MongoDB databases**:

1. **User Service Database** - Stores user accounts, authentication data
2. **Product Service Database** - Stores product information  
3. **Media Service Database** - Stores media file metadata

All databases run in Docker containers and are accessible on your localhost.

---

## 🔌 Connection Details

### 1. User Service Database

**Purpose:** User accounts, authentication, profiles

**Connection String:**
```
mongodb://admin:admin123@localhost:27017/userdb?authSource=admin
```

**Manual Connection:**
- **Host:** `localhost`
- **Port:** `27017`
- **Username:** `admin`
- **Password:** `admin123`
- **Authentication Database:** `admin`
- **Database Name:** `userdb`

**Collections:**
- `user` - User accounts with hashed passwords, emails, roles

---

### 2. Product Service Database

**Purpose:** Product listings created by sellers

**Connection String:**
```
mongodb://admin:admin123@localhost:27018/productdb?authSource=admin
```

**Manual Connection:**
- **Host:** `localhost`
- **Port:** `27018`
- **Username:** `admin`
- **Password:** `admin123`
- **Authentication Database:** `admin`
- **Database Name:** `productdb`

**Collections:**
- `product` - Product information (name, price, stock, seller details)

---

### 3. Media Service Database

**Purpose:** Media file metadata and associations

**Connection String:**
```
mongodb://admin:admin123@localhost:27019/mediadb?authSource=admin
```

**Manual Connection:**
- **Host:** `localhost`
- **Port:** `27019`
- **Username:** `admin`
- **Password:** `admin123`
- **Authentication Database:** `admin`
- **Database Name:** `mediadb`

**Collections:**
- `media` - Media file metadata (filename, size, uploader, product associations)

---

## 🚀 How to Connect in MongoDB Compass

### Method 1: Using Connection String (Recommended)

1. **Open MongoDB Compass**

2. **Click "New Connection"** or use the connection sidebar

3. **Paste one of the connection strings** listed above

4. **Click "Connect"**

5. **Repeat for all three databases** (create separate connections)

### Method 2: Manual Connection Form

1. **Open MongoDB Compass**

2. **Click "New Connection"**

3. **Click "Advanced Connection Options"**

4. **Fill in the following fields:**
   - **General Tab:**
     - Scheme: `mongodb://`
     - Host: `localhost`
     - Port: `27017` (or `27018`, `27019` depending on database)
   
   - **Authentication Tab:**
     - Authentication Method: `Username/Password`
     - Username: `admin`
     - Password: `admin123`
     - Authentication Database: `admin`

5. **Click "Connect"**

---

## 📁 What You'll See

### User Database (`userdb`)
```json
{
  "_id": "675636c3862adf4b36f77a1b",
  "email": "chan.myint@example.com",
  "password": "$2a$10$hashed_password_here",
  "firstName": "Chan",
  "lastName": "Myint",
  "phone": "+1234567890",
  "role": "SELLER",
  "avatarUrl": null,
  "createdAt": "2024-12-09T05:30:27.666",
  "updatedAt": "2024-12-09T05:30:27.666",
  "enabled": true,
  "_class": "com.ecommerce.userservice.model.User"
}
```

### Product Database (`productdb`)
```json
{
  "_id": "69374396b4fff718ae7bd432",
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 Pro chip",
  "price": 999.99,
  "stock": 50,
  "category": "Electronics",
  "sellerId": "675636c3862adf4b36f77a1b",
  "sellerEmail": "chan.myint@example.com",
  "imageIds": ["67564a12b4fff718ae7bd435"],
  "createdAt": "2024-12-09T06:15:34.123",
  "updatedAt": "2024-12-09T06:15:34.123",
  "_class": "com.ecommerce.productservice.model.Product"
}
```

### Media Database (`mediadb`)
```json
{
  "_id": "67564a12b4fff718ae7bd435",
  "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "contentType": "image/jpeg",
  "fileSize": 1048576,
  "uploadedBy": "chan.myint@example.com",
  "productId": "69374396b4fff718ae7bd432",
  "uploadedAt": "2024-12-09T06:15:30.456",
  "_class": "com.ecommerce.mediaservice.model.Media"
}
```

---

## 💡 Tips for Using MongoDB Compass

### View Collections
- After connecting, expand the database in the left sidebar
- Click on any collection to view documents
- Use the **Documents** tab to see all records

### Search/Filter
- Use the filter bar at the top: `{ "email": "chan.myint@example.com" }`
- Filter products by seller: `{ "sellerEmail": "chan.myint@example.com" }`
- Search by price range: `{ "price": { "$gte": 100, "$lte": 500 } }`

### Create Indexes
- Click on the **Indexes** tab
- Useful indexes are already created:
  - User: `email` (unique)
  - Product: `sellerEmail`, `category`

### Export Data
- Click the **Export** button to save data as JSON or CSV
- Useful for backup or analysis

### Delete Documents
- Select documents and click the trash icon
- Use with caution in production!

---

## 🔧 Troubleshooting

### Cannot Connect
**Problem:** Connection refused or timeout

**Solutions:**
1. Check if Docker containers are running:
   ```bash
   docker-compose ps
   ```

2. Restart MongoDB containers if needed:
   ```bash
   docker-compose restart mongodb-user mongodb-product mongodb-media
   ```

3. Check if ports are available:
   ```bash
   lsof -i :27017
   lsof -i :27018
   lsof -i :27019
   ```

### Authentication Failed
**Problem:** Authentication failed or wrong credentials

**Solution:**
- Ensure you're using `admin` as authentication database
- Check username: `admin` and password: `admin123`
- Connection string must include: `?authSource=admin`

### Database Empty
**Problem:** Connected but no collections visible

**Solution:**
- Collections are created automatically when first data is inserted
- Register a user or create a product to see collections appear
- Run the application first, then check Compass

### Wrong Port
**Problem:** Connected to wrong database

**Solution:**
- User Service: Port **27017**
- Product Service: Port **27018**  
- Media Service: Port **27019**
- Double-check the port number in your connection string

---

## 🔐 Security Notes

**⚠️ Important for Production:**

The current setup uses default credentials (`admin:admin123`) which is fine for development but **NOT for production**.

For production deployments:
1. Change MongoDB username and password
2. Use environment variables for credentials
3. Enable SSL/TLS encryption
4. Set up proper network isolation
5. Use MongoDB Atlas for managed hosting
6. Implement IP whitelisting
7. Regular security audits

---

## 📊 Sample Queries

### Find All Sellers
```javascript
// In userdb
{ "role": "SELLER" }
```

### Find Products Under $100
```javascript
// In productdb
{ "price": { "$lt": 100 } }
```

### Find Products by Category
```javascript
// In productdb
{ "category": "Electronics" }
```

### Find Low Stock Products
```javascript
// In productdb
{ "stock": { "$lt": 10 } }
```

### Find Media by Uploader
```javascript
// In mediadb
{ "uploadedBy": "chan.myint@example.com" }
```

---

## 🎯 Quick Access Links

Once connected in MongoDB Compass, you can save these connections as **Favorites** for quick access:

- 🔵 **User DB** - localhost:27017
- 🟢 **Product DB** - localhost:27018
- 🟡 **Media DB** - localhost:27019

Click the ⭐ star icon next to each connection to save it!

---

## 🔄 Data Relationships

Understanding how data connects across databases:

```
User (userdb)
  └─ email: "chan.myint@example.com"
       │
       ├─> Product (productdb)
       │     └─ sellerEmail: "chan.myint@example.com"
       │           └─ imageIds: ["67564a12..."]
       │                 │
       │                 └─> Media (mediadb)
       │                       └─ _id: "67564a12..."
       │
       └─> Media (mediadb)
             └─ uploadedBy: "chan.myint@example.com"
```

---

**Happy Data Exploring! 🎉**

If you need to reset all databases:
```bash
docker-compose down -v
docker-compose up -d
```
