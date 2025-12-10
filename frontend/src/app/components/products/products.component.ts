import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { MediaService } from '../../services/media.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  loading = true;
  errorMessage = '';

  constructor(
    private productService: ProductService,
    public mediaService: MediaService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.getAllProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load products';
        this.loading = false;
      }
    });
  }

  getProductImage(product: Product): string {
    if (product.imageIds && product.imageIds.length > 0) {
      return this.mediaService.getMediaUrl(product.imageIds[0]);
    }
    return 'assets/placeholder.jpg';
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  goToSellerProfile(sellerId: string): void {
    // Get the seller from products array to pass email
    const product = this.products.find(p => p.sellerId === sellerId);
    if (product) {
      this.router.navigate(['/seller', sellerId], { queryParams: { email: product.sellerEmail } });
    }
  }

  getSellerAvatar(product: Product): string {
    if (product.sellerAvatar) {
      // Convert HTTPS URLs to use proxy to avoid certificate errors
      if (product.sellerAvatar.startsWith('https://localhost:8083')) {
        return product.sellerAvatar.replace('https://localhost:8083', '');
      }
      return product.sellerAvatar;
    }
    return '';
  }
}
