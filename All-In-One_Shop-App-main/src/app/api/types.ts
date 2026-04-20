// API Response Types matching Spring Boot DTOs

export interface Store {
  id: string;
  name: string;
  website: string;
  logoUrl?: string;
}

export interface Brand {
  id: string;
  name: string;
  logoUrl?: string;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
}

export interface ProductPrice {
  id: string;
  store: Store;
  price: number;
  originalPrice?: number;
  currency: string;
  productUrl: string;
  inStock: boolean;
  savings?: number;
}

export interface Product {
  id: string;
  name: string;
  description?: string;
  imageUrl?: string;
  additionalImages?: string[];
  sizes?: string[];
  colors?: string[];
  brand?: Brand;
  category?: Category;
  prices: ProductPrice[];
  lowestPrice?: number;
  highestPrice?: number;
  storeCount: number;
}

export interface User {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  avatarUrl?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface SearchRequest {
  query?: string;
  brandIds?: string[];
  categoryIds?: string[];
  storeIds?: string[];
  minPrice?: number;
  maxPrice?: number;
  sizes?: string[];
  colors?: string[];
  sortBy?: 'price_asc' | 'price_desc' | 'name_asc' | 'name_desc';
  page?: number;
  size?: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  avatarUrl?: string;
}
