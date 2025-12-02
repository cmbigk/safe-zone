import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { MediaService, MediaResponse } from '../../services/media.service';
import { AuthService } from '../../services/auth.service';
import { Product, ProductRequest } from '../../models/product.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  products: Product[] = [];
  showForm = false;
  editMode = false;
  currentProductId: string | null = null;
  loading = false;
  errorMessage = '';
  successMessage = '';

  productForm: ProductRequest = {
    name: '',
    description: '',
    price: 0,
    category: '',
    stock: 0,
    imageIds: []
  };

  selectedFile: File | null = null;
  uploadingImage = false;

  constructor(
    private productService: ProductService,
    private mediaService: MediaService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isSeller()) {
      this.router.navigate(['/products']);
      return;
    }
    this.loadMyProducts();
  }

  loadMyProducts(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.productService.getProductsBySeller(user.id).subscribe({
        next: (products) => {
          this.products = products;
        },
        error: () => {
          this.errorMessage = 'Failed to load products';
        }
      });
    }
  }

  openCreateForm(): void {
    this.showForm = true;
    this.editMode = false;
    this.resetForm();
  }

  openEditForm(product: Product): void {
    this.showForm = true;
    this.editMode = true;
    this.currentProductId = product.id;
    this.productForm = {
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
      stock: product.stock,
      imageIds: product.imageIds
    };
  }

  closeForm(): void {
    this.showForm = false;
    this.resetForm();
  }

  resetForm(): void {
    this.productForm = {
      name: '',
      description: '',
      price: 0,
      category: '',
      stock: 0,
      imageIds: []
    };
    this.selectedFile = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        this.errorMessage = 'File size must be less than 2MB';
        return;
      }
      if (!file.type.startsWith('image/')) {
        this.errorMessage = 'Only image files are allowed';
        return;
      }
      this.selectedFile = file;
      this.errorMessage = '';
    }
  }

  uploadImage(): void {
    if (!this.selectedFile) return;

    this.uploadingImage = true;
    this.mediaService.uploadMedia(this.selectedFile).subscribe({
      next: (response: MediaResponse) => {
        this.productForm.imageIds = this.productForm.imageIds || [];
        this.productForm.imageIds.push(response.id);
        this.successMessage = 'Image uploaded successfully';
        this.selectedFile = null;
        this.uploadingImage = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to upload image';
        this.uploadingImage = false;
      }
    });
  }

  onSubmit(): void {
    if (!this.productForm.name || !this.productForm.description || this.productForm.price <= 0) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    if (this.editMode && this.currentProductId) {
      this.productService.updateProduct(this.currentProductId, this.productForm).subscribe({
        next: () => {
          this.successMessage = 'Product updated successfully';
          this.loading = false;
          this.loadMyProducts();
          this.closeForm();
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Failed to update product';
          this.loading = false;
        }
      });
    } else {
      this.productService.createProduct(this.productForm).subscribe({
        next: () => {
          this.successMessage = 'Product created successfully';
          this.loading = false;
          this.loadMyProducts();
          this.closeForm();
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Failed to create product';
          this.loading = false;
        }
      });
    }
  }

  deleteProduct(id: string): void {
    if (!confirm('Are you sure you want to delete this product?')) return;

    this.productService.deleteProduct(id).subscribe({
      next: () => {
        this.successMessage = 'Product deleted successfully';
        this.loadMyProducts();
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to delete product';
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  viewAllProducts(): void {
    this.router.navigate(['/products']);
  }
}
