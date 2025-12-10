import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  editMode = false;
  loading = false;
  successMessage = '';
  errorMessage = '';
  selectedAvatarFile: File | null = null;
  avatarPreview: string | null = null;
  uploadingAvatar = false;

  profileForm = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    avatarUrl: ''
  };

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getCurrentUser();
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    if (this.user) {
      this.profileForm = {
        firstName: this.user.firstName,
        lastName: this.user.lastName,
        email: this.user.email,
        phone: this.user.phone || '',
        avatarUrl: this.user.avatarUrl || ''
      };
    }
  }

  toggleEditMode(): void {
    this.editMode = !this.editMode;
    if (!this.editMode) {
      this.loadUserProfile(); // Reset form if cancelled
      this.selectedAvatarFile = null;
      this.avatarPreview = null;
    }
  }

  onAvatarSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedAvatarFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.avatarPreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  updateProfile(): void {
    if (!this.user) return;

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // If avatar is selected, upload it first
    if (this.selectedAvatarFile) {
      this.uploadingAvatar = true;
      this.authService.uploadAvatar(this.selectedAvatarFile, this.user.email).subscribe({
        next: (avatarResponse) => {
          this.profileForm.avatarUrl = `/api/media/files/${avatarResponse.id}`;
          this.saveProfile();
        },
        error: (error) => {
          this.loading = false;
          this.uploadingAvatar = false;
          this.errorMessage = 'Failed to upload avatar. Please try again.';
        }
      });
    } else {
      this.saveProfile();
    }
  }

  private saveProfile(): void {
    if (!this.user) return;

    this.http.put<User>(`/api/users/${this.user.id}`, this.profileForm).subscribe({
      next: (updatedUser) => {
        // Update seller information in all products if user is a seller
        if (this.isSeller()) {
          const sellerName = `${updatedUser.firstName} ${updatedUser.lastName}`.trim();
          const sellerAvatar = updatedUser.avatarUrl || '';
          
          this.http.put(`/api/products/seller/${updatedUser.email}`, null, {
            params: {
              sellerName: sellerName,
              sellerAvatar: sellerAvatar
            }
          }).subscribe({
            next: () => {
              this.completeProfileUpdate(updatedUser);
            },
            error: (error) => {
              console.error('Failed to update seller products:', error);
              // Still complete the profile update even if product update fails
              this.completeProfileUpdate(updatedUser);
            }
          });
        } else {
          this.completeProfileUpdate(updatedUser);
        }
      },
      error: (error) => {
        this.loading = false;
        this.uploadingAvatar = false;
        this.errorMessage = error.error?.message || 'Failed to update profile';
      }
    });
  }
  
  private completeProfileUpdate(updatedUser: User): void {
    this.loading = false;
    this.uploadingAvatar = false;
    this.successMessage = 'Profile updated successfully!';
    this.editMode = false;
    this.selectedAvatarFile = null;
    this.avatarPreview = null;
    
    // Update user in localStorage and auth service
    localStorage.setItem('user', JSON.stringify(updatedUser));
    this.authService.updateCurrentUser(updatedUser);
    this.user = updatedUser;
    
    // Reload the profile form with updated data
    this.loadUserProfile();
    
    // Clear success message after 3 seconds
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }

  getAvatarUrl(): string {
    if (this.avatarPreview) {
      return this.avatarPreview;
    }
    if (this.profileForm.avatarUrl) {
      // Convert HTTPS URLs to use proxy to avoid certificate errors
      if (this.profileForm.avatarUrl.startsWith('https://localhost:8083')) {
        return this.profileForm.avatarUrl.replace('https://localhost:8083', '');
      }
      return this.profileForm.avatarUrl;
    }
    return '';
  }

  getInitials(): string {
    const firstName = this.profileForm.firstName || '';
    const lastName = this.profileForm.lastName || '';
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  isSeller(): boolean {
    return this.user?.role === 'SELLER';
  }
}
