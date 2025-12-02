# Frontend Testing Guide

## ✅ All Services Running

**Backend Services:**
- User Service: http://localhost:8081
- Product Service: http://localhost:8082
- Media Service: http://localhost:8083
- MongoDB (User): localhost:27017
- MongoDB (Product): localhost:27018
- MongoDB (Media): localhost:27019

**Frontend:**
- Angular App: http://localhost:4200

## 🧪 Manual Test Scenarios

### Test 1: User Registration (as Seller)
1. Open http://localhost:4200
2. Click "Register" in navigation
3. Fill in the form:
   - Username: `testseller`
   - Email: `testseller@example.com`
   - Password: `password123`
   - Role: Select "Seller"
   - Full Name: `Test Seller`
   - Phone: `123-456-7890`
4. Click "Register"
5. ✅ You should be redirected to Dashboard

### Test 2: Create a Product
1. From Dashboard, click "+ Create New Product"
2. Fill in the form:
   - Product Name: `Test Product`
   - Description: `This is a test product`
   - Price: `99.99`
   - Stock: `50`
   - Category: `Electronics`
3. Upload an image (max 2MB, images only)
4. Click "Create"
5. ✅ Product should appear in your products list

### Test 3: View All Products
1. Click "View All Products" or navigate to /products
2. ✅ You should see the product you just created

### Test 4: Register as Client
1. Logout (if logged in)
2. Click "Register"
3. Fill in with different details:
   - Username: `testclient`
   - Email: `testclient@example.com`
   - Password: `password123`
   - Role: Select "Client (Buyer)"
4. ✅ Should redirect to Products page (no Dashboard for clients)

### Test 5: Edit Product (Seller Only)
1. Login as seller (testseller@example.com)
2. Go to Dashboard
3. Click "Edit" on a product
4. Change the price or description
5. Click "Update"
6. ✅ Changes should be saved

### Test 6: Delete Product (Seller Only)
1. From Dashboard, click "Delete" on a product
2. Confirm deletion
3. ✅ Product should be removed

### Test 7: Image Upload Validation
1. Login as seller
2. Try to upload a file > 2MB
3. ✅ Should show error message
4. Try to upload a non-image file (PDF, TXT)
5. ✅ Should show error message

### Test 8: Authorization (User can only modify own products)
1. Create product as testseller
2. Note the product ID
3. Try to edit/delete using different seller account
4. ✅ Should get authorization error

## 🎯 What to Check

✅ **UI/UX:**
- Navbar shows correct links based on role
- Forms have validation
- Error messages display properly
- Success messages show after operations
- Loading states work

✅ **Functionality:**
- Registration works for both roles
- Login returns JWT token
- Products CRUD operations work
- Image upload enforces 2MB limit
- Only sellers can create/edit/delete products
- Clients can only view products

✅ **Security:**
- JWT token stored in localStorage
- Authorization header sent with requests
- Proxy routes API calls correctly
- CORS handled by proxy

## 🐛 Troubleshooting

**If products don't load:**
```powershell
# Check backend logs
docker logs product-service
```

**If login fails:**
```powershell
# Check user service logs
docker logs user-service
```

**If image upload fails:**
```powershell
# Check media service logs
docker logs media-service
```

**To restart all services:**
```powershell
cd "C:\Users\HP Victus 15\Desktop\buy-01"
docker-compose restart
```

**To view database:**
```powershell
# Connect to MongoDB
docker exec -it mongodb-user mongosh
use userdb
db.users.find().pretty()
```

## 📊 Expected Test Results

All tests should pass with:
- ✅ Successful registration/login
- ✅ JWT authentication working
- ✅ Products CRUD working
- ✅ Image upload with 2MB validation
- ✅ Role-based access control
- ✅ Proper error handling
- ✅ Responsive UI

## 🎉 Success Criteria

Your frontend is working correctly if:
1. You can register as both Seller and Client
2. Sellers can create, edit, and delete their own products
3. All users can view product listings
4. Image upload enforces 2MB limit and file type
5. Navigation changes based on user role
6. All error messages display correctly
7. JWT authentication persists across page refreshes
