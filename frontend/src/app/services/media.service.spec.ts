import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MediaService, MediaResponse } from './media.service';

/**
 * Unit tests for MediaService
 * Tests media upload and URL generation functionality
 */
describe('MediaService', () => {
  let service: MediaService;
  let httpMock: HttpTestingController;

  const mockMediaResponse: MediaResponse = {
    id: 'media123',
    filename: 'test-image.jpg',
    contentType: 'image/jpeg',
    fileSize: 1024000,
    uploadedBy: 'user@example.com',
    productId: 'product123',
    uploadedAt: '2024-01-01T00:00:00'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MediaService]
    });

    service = TestBed.inject(MediaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should upload media file with productId', (done) => {
    // Arrange
    const mockFile = new File(['test content'], 'test-image.jpg', { type: 'image/jpeg' });
    const userEmail = 'user@example.com';
    const productId = 'product123';

    // Act
    service.uploadMedia(mockFile, userEmail, productId).subscribe(response => {
      // Assert
      expect(response).toEqual(mockMediaResponse);
      expect(response.filename).toBe('test-image.jpg');
      expect(response.productId).toBe('product123');
      done();
    });

    const req = httpMock.expectOne('/api/media/upload');
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('X-User-Email')).toBe(userEmail);
    expect(req.request.body instanceof FormData).toBe(true);
    req.flush(mockMediaResponse);
  });

  it('should upload media file without productId', (done) => {
    // Arrange
    const mockFile = new File(['test content'], 'avatar.jpg', { type: 'image/jpeg' });
    const userEmail = 'user@example.com';
    const responseWithoutProduct = { ...mockMediaResponse, productId: undefined };

    // Act
    service.uploadMedia(mockFile, userEmail).subscribe(response => {
      // Assert
      expect(response).toEqual(responseWithoutProduct);
      expect(response.productId).toBeUndefined();
      done();
    });

    const req = httpMock.expectOne('/api/media/upload');
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('X-User-Email')).toBe(userEmail);
    req.flush(responseWithoutProduct);
  });

  it('should generate correct media URL', () => {
    // Act
    const url = service.getMediaUrl('test-image.jpg');

    // Assert
    expect(url).toBe('/api/media/files/test-image.jpg');
  });

  it('should generate media URL with special characters', () => {
    // Act
    const url = service.getMediaUrl('test image with spaces.jpg');

    // Assert
    expect(url).toBe('/api/media/files/test image with spaces.jpg');
  });

  it('should handle upload errors correctly', (done) => {
    // Arrange
    const mockFile = new File(['test'], 'large-file.jpg', { type: 'image/jpeg' });
    const userEmail = 'user@example.com';
    const errorMessage = 'File size exceeds maximum allowed';

    // Act
    service.uploadMedia(mockFile, userEmail).subscribe({
      next: () => fail('should have failed'),
      error: (error) => {
        // Assert
        expect(error.status).toBe(400);
        expect(error.error).toBe(errorMessage);
        done();
      }
    });

    const req = httpMock.expectOne('/api/media/upload');
    req.flush(errorMessage, { status: 400, statusText: 'Bad Request' });
  });
});
