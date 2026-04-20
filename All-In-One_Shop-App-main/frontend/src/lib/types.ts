// TypeScript interfaces matching the backend API
// ================================================

export interface Store {
  id: string;
  name: string;
  website: string;
  logoUrl?: string;
  isActive: boolean;
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
  imageUrl?: string;
}

export interface ProductPrice {
  id: string;
  store: Store;
  price: number;
  originalPrice?: number;
  currency: string;
  productUrl: string;
  inStock: boolean;
}

export interface Product {
  id: string;
  name: string;
  description?: string;
  imageUrl?: string;
  additionalImages?: string[];
  sizes?: string[];
  colors?: string[];
  gender?: 'MEN' | 'WOMEN' | 'UNISEX';
  brand?: Brand;
  category?: Category;
  prices: ProductPrice[];
  lowestPrice?: number;
  highestPrice?: number;
  storeCount: number;
  isActive: boolean;
}

export interface User {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: 'USER' | 'ADMIN';
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

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface SearchParams {
  query?: string;
  brandId?: string;
  categoryId?: string;
  minPrice?: number;
  maxPrice?: number;
  gender?: string;
  sortBy?: 'price_asc' | 'price_desc' | 'name_asc' | 'newest';
  page?: number;
  size?: number;
}

export interface DashboardStats {
  totalProducts: number;
  activeStores: number;
  totalBrands: number;
  totalPrices: number;
  avgPrice: number;
  topPriceProduct: string;
}
