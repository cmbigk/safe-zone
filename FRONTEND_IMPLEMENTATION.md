# Frontend Implementation Guide

## Overview
The frontend requires an Angular application with the following pages:
1. Sign-in/Sign-up pages (with role selection)
2. Seller Dashboard (product management + image upload)
3. Product Listing (public view)
4. Media Management (seller only)

## Quick Setup (Using Angular CLI)

### Prerequisites
```powershell
# Install Node.js (v18 or later)
# Install Angular CLI
npm install -g @angular/cli@17
```

### Create Angular Project
```powershell
cd "C:\Users\HP Victus 15\Desktop\buy-01"
ng new frontend --routing --style=scss
cd frontend
```

### Install Dependencies
```powershell
npm install @angular/common @angular/forms
npm install @angular/material @angular/cdk
npm install rxjs
```

### Project Structure
```
frontend/
├── src/
│   ├── app/
│   │   ├── auth/
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   └── auth.service.ts
│   │   ├── products/
│   │   │   ├── product-list/
│   │   │   ├── product-form/
│   │   │   └── product.service.ts
│   │   ├── media/
│   │   │   ├── media-upload/
│   │   │   └── media.service.ts
│   │   ├── dashboard/
│   │   │   └── seller-dashboard/
│   │   └── shared/
│   │       ├── models/
│   │       └── guards/
│   ├── environments/
│   └── index.html
└── angular.json
```

## Key Components to Implement

### 1. Auth Service (`src/app/auth/auth.service.ts`)
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/auth';
  private currentUserSubject = new BehaviorSubject<any>(null);
  
  constructor(private http: HttpClient) {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    if (token && user) {
      this.currentUserSubject.next(JSON.parse(user));
    }
  }
  
  register(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data)
      .pipe(tap(response => this.setSession(response)));
  }
  
  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password })
      .pipe(tap(response => this.setSession(response)));
  }
  
  private setSession(response: AuthResponse): void {
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify(response));
    this.currentUserSubject.next(response);
  }
  
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }
  
  getToken(): string | null {
    return localStorage.getItem('token');
  }
  
  isSeller(): boolean {
    const user = this.currentUserSubject.value;
    return user && user.role === 'SELLER';
  }
  
  getCurrentUser(): Observable<any> {
    return this.currentUserSubject.asObservable();
  }
}
```

### 2. HTTP Interceptor (JWT Token)
```typescript
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler } from '@angular/common/http';
import { AuthService } from './auth/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}
  
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = this.authService.getToken();
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    return next.handle(req);
  }
}
```

### 3. Register Component (`src/app/auth/register/register.component.ts`)
```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-register',
  template: `
    <div class="register-container">
      <h2>Register</h2>
      <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
        <input formControlName="email" placeholder="Email" type="email" required>
        <input formControlName="password" placeholder="Password" type="password" required>
        <input formControlName="firstName" placeholder="First Name" required>
        <input formControlName="lastName" placeholder="Last Name" required>
        <input formControlName="phone" placeholder="Phone" required>
        
        <label>
          <input type="radio" formControlName="role" value="CLIENT"> Client
        </label>
        <label>
          <input type="radio" formControlName="role" value="SELLER"> Seller
        </label>
        
        <button type="submit" [disabled]="!registerForm.valid">Register</button>
        <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
      </form>
    </div>
  `
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage = '';
  
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phone: ['', Validators.required],
      role: ['CLIENT', Validators.required]
    });
  }
  
  onSubmit(): void {
    if (this.registerForm.valid) {
      this.authService.register(this.registerForm.value).subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.errorMessage = err.error.message || 'Registration failed';
        }
      });
    }
  }
}
```

### 4. Product Service (`src/app/products/product.service.ts`)
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private apiUrl = 'http://localhost:8082/api/products';
  
  constructor(private http: HttpClient) {}
  
  getAllProducts(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
  
  getProductById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }
  
  createProduct(product: any, email: string, userId: string): Observable<any> {
    const headers = new HttpHeaders()
      .set('X-User-Email', email)
      .set('X-User-Id', userId);
    return this.http.post<any>(this.apiUrl, product, { headers });
  }
  
  updateProduct(id: string, product: any, email: string): Observable<any> {
    const headers = new HttpHeaders().set('X-User-Email', email);
    return this.http.put<any>(`${this.apiUrl}/${id}`, product, { headers });
  }
  
  deleteProduct(id: string, email: string): Observable<void> {
    const headers = new HttpHeaders().set('X-User-Email', email);
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers });
  }
}
```

### 5. Media Upload Component (`src/app/media/media-upload/media-upload.component.ts`)
```typescript
import { Component } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-media-upload',
  template: `
    <div class="upload-container">
      <h3>Upload Product Image</h3>
      <input type="file" (change)="onFileSelected($event)" accept="image/*">
      <input type="text" [(ngModel)]="productId" placeholder="Product ID (optional)">
      <button (click)="upload()" [disabled]="!selectedFile">Upload</button>
      
      <div *ngIf="uploadProgress">Progress: {{ uploadProgress }}%</div>
      <div *ngIf="uploadedUrl">
        <p>Uploaded successfully!</p>
        <img [src]="uploadedUrl" alt="Uploaded image" style="max-width: 300px;">
      </div>
      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
    </div>
  `
})
export class MediaUploadComponent {
  selectedFile: File | null = null;
  productId = '';
  uploadProgress = 0;
  uploadedUrl = '';
  errorMessage = '';
  
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}
  
  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
    this.errorMessage = '';
    
    // Validate file size (2MB)
    if (this.selectedFile && this.selectedFile.size > 2 * 1024 * 1024) {
      this.errorMessage = 'File size exceeds 2MB limit';
      this.selectedFile = null;
    }
  }
  
  upload(): void {
    if (!this.selectedFile) return;
    
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    if (this.productId) {
      formData.append('productId', this.productId);
    }
    
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const headers = new HttpHeaders().set('X-User-Email', user.email);
    
    this.http.post('http://localhost:8083/api/media/upload', formData, { headers })
      .subscribe({
        next: (response: any) => {
          this.uploadedUrl = `http://localhost:8083${response.url}`;
          this.uploadProgress = 100;
        },
        error: (err) => {
          this.errorMessage = err.error.message || 'Upload failed';
        }
      });
  }
}
```

### 6. Seller Dashboard (`src/app/dashboard/seller-dashboard/seller-dashboard.component.ts`)
```typescript
import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../products/product.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-seller-dashboard',
  template: `
    <div class="dashboard">
      <h2>Seller Dashboard</h2>
      
      <div class="product-form">
        <h3>{{ editMode ? 'Edit Product' : 'Add New Product' }}</h3>
        <input [(ngModel)]="product.name" placeholder="Product Name">
        <textarea [(ngModel)]="product.description" placeholder="Description"></textarea>
        <input [(ngModel)]="product.price" type="number" placeholder="Price">
        <input [(ngModel)]="product.stock" type="number" placeholder="Stock">
        <input [(ngModel)]="product.category" placeholder="Category">
        <button (click)="saveProduct()">{{ editMode ? 'Update' : 'Create' }}</button>
        <button *ngIf="editMode" (click)="cancelEdit()">Cancel</button>
      </div>
      
      <div class="product-list">
        <h3>My Products</h3>
        <div *ngFor="let p of products" class="product-item">
          <h4>{{ p.name }}</h4>
          <p>{{ p.description }}</p>
          <p>Price: ${{ p.price }} | Stock: {{ p.stock }}</p>
          <button (click)="editProduct(p)">Edit</button>
          <button (click)="deleteProduct(p.id)">Delete</button>
        </div>
      </div>
      
      <app-media-upload></app-media-upload>
    </div>
  `
})
export class SellerDashboardComponent implements OnInit {
  products: any[] = [];
  product: any = { name: '', description: '', price: 0, stock: 0, category: '' };
  editMode = false;
  
  constructor(
    private productService: ProductService,
    private authService: AuthService
  ) {}
  
  ngOnInit(): void {
    this.loadProducts();
  }
  
  loadProducts(): void {
    // Load seller's products
    this.productService.getAllProducts().subscribe(products => {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      this.products = products.filter(p => p.sellerEmail === user.email);
    });
  }
  
  saveProduct(): void {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (this.editMode) {
      this.productService.updateProduct(this.product.id, this.product, user.email)
        .subscribe(() => {
          this.loadProducts();
          this.cancelEdit();
        });
    } else {
      this.productService.createProduct(this.product, user.email, user.userId)
        .subscribe(() => {
          this.loadProducts();
          this.product = { name: '', description: '', price: 0, stock: 0, category: '' };
        });
    }
  }
  
  editProduct(product: any): void {
    this.product = { ...product };
    this.editMode = true;
  }
  
  deleteProduct(id: string): void {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.productService.deleteProduct(id, user.email).subscribe(() => {
      this.loadProducts();
    });
  }
  
  cancelEdit(): void {
    this.product = { name: '', description: '', price: 0, stock: 0, category: '' };
    this.editMode = false;
  }
}
```

### 7. Product Listing (Public) (`src/app/products/product-list/product-list.component.ts`)
```typescript
import { Component, OnInit } from '@angular/core';
import { ProductService } from '../product.service';

@Component({
  selector: 'app-product-list',
  template: `
    <div class="product-listing">
      <h2>All Products</h2>
      <div class="products-grid">
        <div *ngFor="let product of products" class="product-card">
          <h3>{{ product.name }}</h3>
          <p>{{ product.description }}</p>
          <p class="price">\${{ product.price }}</p>
          <p class="stock">Stock: {{ product.stock }}</p>
          <p class="category">Category: {{ product.category }}</p>
          <div *ngIf="product.imageIds && product.imageIds.length">
            <img *ngFor="let imgId of product.imageIds" 
                 [src]="'http://localhost:8083/api/media/files/' + imgId" 
                 alt="Product image" 
                 style="max-width: 200px;">
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .products-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
    }
    .product-card {
      border: 1px solid #ccc;
      padding: 15px;
      border-radius: 8px;
    }
  `]
})
export class ProductListComponent implements OnInit {
  products: any[] = [];
  
  constructor(private productService: ProductService) {}
  
  ngOnInit(): void {
    this.productService.getAllProducts().subscribe(products => {
      this.products = products;
    });
  }
}
```

### 8. App Routing (`src/app/app-routing.module.ts`)
```typescript
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { SellerDashboardComponent } from './dashboard/seller-dashboard/seller-dashboard.component';
import { ProductListComponent } from './products/product-list/product-list.component';

const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'dashboard', component: SellerDashboardComponent },
  { path: '**', redirectTo: '/products' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
```

## Build & Run

### Development Server
```powershell
cd frontend
ng serve
```

Access at: http://localhost:4200

### Docker Build
Create `frontend/Dockerfile`:
```dockerfile
# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=build /app/dist/frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Create `frontend/nginx.conf`:
```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://api-gateway:8080;
    }
}
```

### Build Docker Image
```powershell
cd frontend
docker build -t frontend:latest .
```

## Testing Checklist

- [ ] Register as CLIENT - verify role selection works
- [ ] Register as SELLER - verify role selection works
- [ ] Login with both roles
- [ ] View product listing (public, no auth)
- [ ] As SELLER: Create product
- [ ] As SELLER: Upload image for product
- [ ] As SELLER: Edit own product
- [ ] As SELLER: Delete own product
- [ ] As CLIENT: Try to access seller dashboard (should redirect)
- [ ] File upload validation (2MB limit, image types only)
- [ ] Error messages display correctly
- [ ] JWT token stored and sent with requests

## Notes

- All API calls go directly to backend services (ports 8081, 8082, 8083)
- In production, use API Gateway (port 8080) as single entry point
- Enable CORS in backend services for development
- Use environment files for API URLs
- Add loading spinners and better error handling
- Implement route guards for seller-only pages
- Add avatar upload in profile page
- Add product search and filtering
