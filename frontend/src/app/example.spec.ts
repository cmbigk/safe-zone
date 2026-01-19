/**
 * Example Jasmine/Karma Test Suite
 * 
 * This demonstrates best practices for frontend testing in the pipeline.
 * Tests run with 'npm run test:ci' and generate XML reports in test-results/
 * 
 * Key Testing Patterns:
 * - Use describe() for test suites
 * - Use it() for individual test cases
 * - Use beforeEach() for setup
 * - Use afterEach() for cleanup
 * - Test user interactions and component state
 * - Mock dependencies and services
 */

describe('Example Service', () => {
  let service: any;

  beforeEach(() => {
    // Arrange: Initialize service before each test
    service = {
      data: 'Hello, World!',
      numbers: [1, 2, 3, 4, 5],
      isValid: true,
      
      getData(): string {
        return this.data;
      },
      
      setData(value: string): void {
        this.data = value;
      },
      
      calculate(a: number, b: number): number {
        return a + b;
      },
      
      getNumbers(): number[] {
        return this.numbers;
      }
    };
  });

  afterEach(() => {
    // Cleanup after each test
    service = null;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return initial data', () => {
    // Act
    const result = service.getData();
    
    // Assert
    expect(result).toBe('Hello, World!');
    expect(result).toContain('Hello');
  });

  it('should update data correctly', () => {
    // Arrange
    const newData = 'Updated Data';
    
    // Act
    service.setData(newData);
    
    // Assert
    expect(service.getData()).toBe(newData);
  });

  it('should perform calculations correctly', () => {
    // Act
    const sum = service.calculate(5, 10);
    
    // Assert
    expect(sum).toBe(15);
    expect(sum).toBeGreaterThan(0);
  });

  it('should handle array operations', () => {
    // Act
    const numbers = service.getNumbers();
    
    // Assert
    expect(numbers).toBeDefined();
    expect(numbers.length).toBe(5);
    expect(numbers[0]).toBe(1);
    expect(numbers[4]).toBe(5);
  });

  it('should validate boolean properties', () => {
    // Assert
    expect(service.isValid).toBeTruthy();
    expect(service.isValid).toBe(true);
  });
});

describe('Example Component', () => {
  
  it('should handle user clicks', () => {
    // Arrange
    let clicked = false;
    const component = {
      onClick(): void {
        clicked = true;
      }
    };
    
    // Act
    component.onClick();
    
    // Assert
    expect(clicked).toBe(true);
  });

  it('should validate form input', () => {
    // Arrange
    const component = {
      email: '',
      
      isValidEmail(): boolean {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(this.email);
      }
    };
    
    // Act & Assert - Invalid email
    component.email = 'invalid-email';
    expect(component.isValidEmail()).toBe(false);
    
    // Act & Assert - Valid email
    component.email = 'test@example.com';
    expect(component.isValidEmail()).toBe(true);
  });

  it('should handle asynchronous operations', (done) => {
    // Arrange
    const asyncFunction = (): Promise<string> => {
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve('Async Result');
        }, 100);
      });
    };
    
    // Act
    asyncFunction().then((result) => {
      // Assert
      expect(result).toBe('Async Result');
      done();
    });
  });

  it('should handle errors gracefully', () => {
    // Arrange
    const component = {
      divide(a: number, b: number): number {
        if (b === 0) {
          throw new Error('Division by zero');
        }
        return a / b;
      }
    };
    
    // Assert - Should throw error
    expect(() => component.divide(10, 0)).toThrow();
    expect(() => component.divide(10, 0)).toThrowError('Division by zero');
    
    // Assert - Should not throw error
    expect(() => component.divide(10, 2)).not.toThrow();
    expect(component.divide(10, 2)).toBe(5);
  });
});

describe('Example Integration Tests', () => {
  
  it('should integrate multiple components', () => {
    // Arrange
    const dataService = {
      value: 0,
      increment(): void {
        this.value++;
      },
      getValue(): number {
        return this.value;
      }
    };
    
    const component = {
      count: 0,
      service: dataService,
      
      incrementCount(): void {
        this.count++;
        this.service.increment();
      }
    };
    
    // Act
    component.incrementCount();
    component.incrementCount();
    
    // Assert
    expect(component.count).toBe(2);
    expect(component.service.getValue()).toBe(2);
  });
});
