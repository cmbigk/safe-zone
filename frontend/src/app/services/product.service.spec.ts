import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductService } from './product.service';
import { AuthService } from './auth.service';
import { Product, ProductRequest } from '../models/product.model';
import { User } from '../models/user.model';

/**
 * Unit tests for ProductService
 * Tests product CRUD operations and seller-specific functionality
 */
describe('ProductService - Real Tests', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;

  const mockUser: User = {
    id: 'seller123',
    email: 'seller@example.com',
    firstName: 'John',
    lastName: 'Seller',
    phone: '1234567890',
    role: 'SELLER',
    avatarUrl: '/avatar.jpg',
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  };

  const mockProduct: Product = {
    id: 'product123',
    name: 'Test Product',
    description: 'Test Description',
    price: 99.99,
    stock: 100,
    category: 'Electronics',
    sellerId: 'seller123',
    sellerEmail: 'seller@example.com',
    sellerName: 'John Seller',
    sellerAvatar: '/avatar.jpg',
    imageIds: ['img1', 'img2'],
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  };

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ProductService,
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });

    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    
    authService.getCurrentUser.and.returnValue(mockUser);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve all products', (done) => {
    // Arrange
    const mockProducts: Product[] = [mockProduct, { ...mockProduct, id: 'product456', name: 'Another Product' }];

    // Act
    service.getAllProducts().subscribe(products => {
      // Assert
      expect(products).toEqual(mockProducts);
      expect(products.length).toBe(2);
      expect(products[0].name).toBe('Test Product');
      done();
    });

    const req = httpMock.expectOne('/api/products');
    expect(req.request.method).toBe('GET');
    req.flush(mockProducts);
  });

  it('should retrieve a single product by ID', (done) => {
    // Act
    service.getProductById('product123').subscribe(product => {
      // Assert
      expect(product).toEqual(mockProduct);
      expect(product.id).toBe('product123');
      expect(product.name).toBe('Test Product');
      done();
    });

    const req = httpMock.expectOne('/api/products/product123');
    expect(req.request.method).toBe('GET');
    req.flush(mockProduct);
  });

  it('should retrieve products by seller email', (done) => {
    // Arrange
    const sellerProducts: Product[] = [mockProduct];

    // Act
    service.getProductsBySeller('seller@example.com').subscribe(products => {
      // Assert
      expect(products).toEqual(sellerProducts);
      expect(products.length).toBe(1);
      expect(products[0].sellerEmail).toBe('seller@example.com');
      done();
    });

    const req = httpMock.expectOne('/api/products/seller/seller@example.com');
    expect(req.request.method).toBe('GET');
    req.flush(sellerProducts);
  });

  it('should retrieve products by category', (done) => {
    // Arrange
    const categoryProducts: Product[] = [mockProduct, { ...mockProduct, id: 'product456' }];

    // Act
    service.getProductsByCategory('Electronics').subscribe(products => {
      // Assert
      expect(products).toEqual(categoryProducts);
      expect(products.length).toBe(2);
      expect(products.every(p => p.category === 'Electronics')).toBe(true);
      done();
    });

    const req = httpMock.expectOne('/api/products/category/Electronics');
    expect(req.request.method).toBe('GET');
    req.flush(categoryProducts);
  });

  it('should create a new product with seller headers', (done) => {
    // Arrange
    const productRequest: ProductRequest = {
      name: 'New Product',
      description: 'New Description',
      price: 149.99,
      stock: 50,
      category: 'Books',
      sellerName: 'John Seller',
      sellerAvatar: '/avatar.jpg',
      imageIds: []
    };

    // Act
    service.createProduct(productRequest).subscribe(product => {
      // Assert
      expect(product.name).toBe('New Product');
      expect(product.price).toBe(149.99);
      done();
    });

    const req = httpMock.expectOne('/api/products');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(productRequest);
    expect(req.request.headers.get('X-User-Email')).toBe('seller@example.com');
    expect(req.request.headers.get('X-User-Id')).toBe('seller123');
    
    req.flush({ ...mockProduct, ...productRequest });
  });

  it('should update an existing product', (done) => {
    // Arrange
    const updateRequest: ProductRequest = {
      name: 'Updated Product',
      description: 'Updated Description',
      price: 199.99,
      stock: 75,
      category: 'Electronics',
      sellerName: 'John Seller',
      sellerAvatar: '/avatar.jpg',
      imageIds: ['img1']
    };

    // Act
    service.updateProduct('product123', updateRequest).subscribe(product => {
      // Assert
      expect(product.name).toBe('Updated Product');
      expect(product.price).toBe(199.99);
      expect(product.stock).toBe(75);
      done();
    });

    const req = httpMock.expectOne('/api/products/product123');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateRequest);
    expect(req.request.headers.get('X-User-Email')).toBe('seller@example.com');
    
    req.flush({ ...mockProduct, ...updateRequest });
  });

  it('should delete a product', (done) => {
    // Act
    service.deleteProduct('product123').subscribe(() => {
      // Assert
      expect(true).toBe(true); // Just verify the call completes
      done();
    });

    const req = httpMock.expectOne('/api/products/product123');
    expect(req.request.method).toBe('DELETE');
    expect(req.request.headers.get('X-User-Email')).toBe('seller@example.com');
    expect(req.request.headers.get('X-User-Id')).toBe('seller123');
    req.flush(null);
  });

  it('should include user headers when user is authenticated', (done) => {
    // Arrange - ensure mock returns user
    authService.getCurrentUser.and.returnValue(mockUser);

    // Act
    service.getAllProducts().subscribe(() => {
      // Assert that getCurrentUser was called
      expect(authService.getCurrentUser).toHaveBeenCalled();
      done();
    });

    // Assert
    const req = httpMock.expectOne('/api/products');
    expect(req.request.headers.has('X-User-Email')).toBe(true);
    expect(req.request.headers.has('X-User-Id')).toBe(true);
    req.flush([]);
  });

  it('should handle requests when user is not authenticated', (done) => {
    // Arrange
    authService.getCurrentUser.and.returnValue(null);

    // Act
    service.getAllProducts().subscribe(() => {
      done();
    });

    // Assert
    const req = httpMock.expectOne('/api/products');
    expect(req.request.headers.has('X-User-Email')).toBe(false);
    expect(req.request.headers.has('X-User-Id')).toBe(false);
    req.flush([]);
  });
})
