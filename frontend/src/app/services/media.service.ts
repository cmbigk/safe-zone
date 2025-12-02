import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MediaResponse {
  id: string;
  filename: string;
  contentType: string;
  fileSize: number;
  uploadedBy: string;
  productId?: string;
  uploadedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private apiUrl = '/api/media';

  constructor(private http: HttpClient) {}

  uploadMedia(file: File, productId?: string): Observable<MediaResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (productId) {
      formData.append('productId', productId);
    }
    
    return this.http.post<MediaResponse>(`${this.apiUrl}/upload`, formData);
  }

  getMediaUrl(filename: string): string {
    return `${this.apiUrl}/files/${filename}`;
  }
}
