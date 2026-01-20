import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { User, UserRole, LoginRequest, RegisterRequest, AuthResponse } from '../models/user.model';

/**
 * Unit tests for AuthService
 * Tests authentication logic, token management, and user state management
 */
describe('AuthService - Real Tests', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUser: User = {
    id: 'user123',
    email: 'test@example.com',
    firstName: 'John',
    lastName: 'Doe',
    phone: '1234567890',
    role: UserRole.BUYER,
    avatarUrl: '/avatar.jpg',
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  };

  const mockAuthResponse: AuthResponse = {
    token: 'jwt-token-123',
    user: mockUser
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    
    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should successfully register a new user', (done) => {
    // Arrange
    const registerRequest: RegisterRequest = {
      email: 'test@example.com',
      password: 'password123',
      firstName: 'John',
      lastName: 'Doe',
      phone: '1234567890',
      role: UserRole.BUYER
    };

    // Act
    service.register(registerRequest).subscribe(response => {
      // Assert
      expect(response).toEqual(mockAuthResponse);
      expect(localStorage.getItem('token')).toBe('jwt-token-123');
      expect(localStorage.getItem('user')).toBeTruthy();
      expect(service.getCurrentUser()?.email).toBe('test@example.com');
      expect(service.isAuthenticated()).toBe(true);
      done();
    });

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(registerRequest);
    req.flush(mockAuthResponse);
  });

  it('should successfully login with valid credentials', (done) => {
    // Arrange
    const loginRequest: LoginRequest = {
      email: 'test@example.com',
      password: 'password123'
    };

    // Act
    service.login(loginRequest).subscribe(response => {
      // Assert
      expect(response).toEqual(mockAuthResponse);
      expect(localStorage.getItem('token')).toBe('jwt-token-123');
      expect(service.isAuthenticated()).toBe(true);
      done();
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(loginRequest);
    req.flush(mockAuthResponse);
  });

  it('should correctly identify seller role', () => {
    // Arrange
    const sellerUser: User = { ...mockUser, role: UserRole.SELLER };
    localStorage.setItem('user', JSON.stringify(sellerUser));
    
    // Reload service to pick up the user from localStorage
    service = TestBed.inject(AuthService);

    // Act & Assert
    expect(service.isSeller()).toBe(true);
  });

  it('should correctly identify buyer role (not seller)', () => {
    // Arrange
    localStorage.setItem('user', JSON.stringify(mockUser));
    service = TestBed.inject(AuthService);

    // Act & Assert
    expect(service.isSeller()).toBe(false);
  });

  it('should clear authentication data on logout', () => {
    // Arrange
    localStorage.setItem('token', 'jwt-token-123');
    localStorage.setItem('user', JSON.stringify(mockUser));

    // Act
    service.logout();

    // Assert
    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should return token from localStorage', () => {
    // Arrange
    localStorage.setItem('token', 'jwt-token-123');

    // Act
    const token = service.getToken();

    // Assert
    expect(token).toBe('jwt-token-123');
  });

  it('should return null when no token exists', () => {
    // Act
    const token = service.getToken();

    // Assert
    expect(token).toBeNull();
  });

  it('should correctly check authentication status', () => {
    // Act & Assert - Not authenticated
    expect(service.isAuthenticated()).toBe(false);

    // Arrange - Set token
    localStorage.setItem('token', 'jwt-token-123');

    // Act & Assert - Authenticated
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should update current user in localStorage and observable', () => {
    // Arrange
    const updatedUser: User = {
      ...mockUser,
      firstName: 'Jane',
      lastName: 'Smith'
    };

    // Act
    service.updateCurrentUser(updatedUser);

    // Assert
    const storedUser = JSON.parse(localStorage.getItem('user') || '{}');
    expect(storedUser.firstName).toBe('Jane');
    expect(storedUser.lastName).toBe('Smith');
    expect(service.getCurrentUser()?.firstName).toBe('Jane');
  });

  it('should emit current user changes through observable', (done) => {
    // Arrange
    let emittedUser: User | null = null;
    
    service.currentUser$.subscribe(user => {
      emittedUser = user;
    });

    // Act
    service.updateCurrentUser(mockUser);

    // Assert
    setTimeout(() => {
      expect(emittedUser).toEqual(mockUser);
      done();
    }, 100);
  });

  it('should load user from localStorage on service initialization', () => {
    // Arrange
    localStorage.setItem('token', 'jwt-token-123');
    localStorage.setItem('user', JSON.stringify(mockUser));

    // Act - Create new service instance
    const newService = new AuthService(null as any);

    // Assert
    expect(newService.getCurrentUser()).toEqual(mockUser);
  });
}
