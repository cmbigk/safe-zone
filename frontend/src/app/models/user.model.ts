export interface User {
  id: string;
  username: string;
  email: string;
  role: 'CLIENT' | 'SELLER';
  fullName?: string;
  phone?: string;
  address?: string;
  avatarUrl?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: 'CLIENT' | 'SELLER';
  fullName?: string;
  phone?: string;
  address?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
