import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-seller-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './seller-profile.component.html',
  styleUrls: ['./seller-profile.component.scss']
})
export class SellerProfileComponent implements OnInit {
  sellerProducts: Product[] = [];
  sellerName: string = '';
  sellerAvatar: string = '';
  sellerEmail: string = '';
  loading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService
  ) {}

  ngOnInit(): void {
    // Get seller email from route params (will be passed as queryParam)
    this.route.queryParams.subscribe(params => {
      const sellerEmail = params['email'];
      if (sellerEmail) {
        this.loadSellerProducts(sellerEmail);
      }
    });

    // Also try to get from route data if available
    this.route.data.subscribe(data => {
      if (data['sellerName']) {
        this.sellerName = data['sellerName'];
      }
      if (data['sellerAvatar']) {
        this.sellerAvatar = data['sellerAvatar'];
      }
    });
  }

  loadSellerProducts(sellerEmail: string): void {
    this.loading = true;
    this.sellerEmail = sellerEmail;
    this.productService.getProductsBySeller(sellerEmail).subscribe({
      next: (products) => {
        this.sellerProducts = products;
        if (products.length > 0) {
          this.sellerName = products[0].sellerName;
          this.sellerAvatar = products[0].sellerAvatar || '';
        }
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load seller products';
        this.loading = false;
      }
    });
  }

  getProductImage(product: Product): string {
    if (product.imageIds && product.imageIds.length > 0) {
      return `/api/media/files/${product.imageIds[0]}`;
    }
    return 'assets/placeholder.jpg';
  }

  getSellerAvatarUrl(): string {
    if (this.sellerAvatar) {
      // Convert HTTPS URLs to use proxy to avoid certificate errors
      if (this.sellerAvatar.startsWith('https://localhost:8083')) {
        return this.sellerAvatar.replace('https://localhost:8083', '');
      }
      return this.sellerAvatar;
    }
    return '';
  }

  goBack(): void {
    this.router.navigate(['/products']);
  }
}
