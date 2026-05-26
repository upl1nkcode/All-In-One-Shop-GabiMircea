// API Client for Spring Boot Backend
import type {
  ApiResponse,
  PagedResponse,
  Product,
  Brand,
  Category,
  Store,
  SearchRequest,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
  AdminStats,
  FakerStatus,
} from './types';

// Base URL for the API - will be replaced with actual backend URL
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Token management
let authToken: string | null = localStorage.getItem('auth_token');

export function setAuthToken(token: string | null) {
  authToken = token;
  if (token) {
    localStorage.setItem('auth_token', token);
  } else {
    localStorage.removeItem('auth_token');
  }
}

export function getAuthToken(): string | null {
  return authToken;
}

export function isAuthenticated(): boolean {
  return !!authToken;
}

// Generic fetch wrapper
async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (authToken) {
    (headers as Record<string, string>)['Authorization'] = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Network error' }));
    throw new Error(error.message || 'Request failed');
  }

  return response.json();
}

// Product API
export const productApi = {
  getAll: (size = 20) => apiRequest<PagedResponse<Product>>(`/products?size=${size}`),

  getById: (id: string) => apiRequest<Product>(`/products/${id}`),

  search: (request: SearchRequest) =>
    apiRequest<PagedResponse<Product>>('/products/search', {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  getByCategory: (slug: string) => apiRequest<Product[]>(`/products/category/${slug}`),

  getByBrand: (name: string) => apiRequest<Product[]>(`/products/brand/${name}`),

  getSimilar: (id: string, limit = 4) =>
    apiRequest<Product[]>(`/products/${id}/similar?limit=${limit}`),

  getTrending: (limit = 8) => apiRequest<Product[]>(`/products/trending?limit=${limit}`),

  update: (id: string, dto: Record<string, unknown>) =>
    apiRequest<Product>(`/products/${id}`, { method: 'PUT', body: JSON.stringify(dto) }),

  remove: (id: string) =>
    apiRequest<void>(`/products/${id}`, { method: 'DELETE' }),
};

// Catalog API (Categories, Brands, Stores)
export const catalogApi = {
  getCategories: () => apiRequest<Category[]>('/categories'),

  getBrands: () => apiRequest<Brand[]>('/brands'),

  getStores: () => apiRequest<Store[]>('/stores'),
};

// Store CRUD API
export const storeApi = {
  create: (dto: Record<string, unknown>) =>
    apiRequest<Store>('/stores', { method: 'POST', body: JSON.stringify(dto) }),

  update: (id: string, dto: Record<string, unknown>) =>
    apiRequest<Store>(`/stores/${id}`, { method: 'PUT', body: JSON.stringify(dto) }),

  remove: (id: string) =>
    apiRequest<void>(`/stores/${id}`, { method: 'DELETE' }),
};

// Auth API
export const authApi = {
  login: async (credentials: LoginRequest) => {
    const response = await apiRequest<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    if (response.success && response.data.token) {
      setAuthToken(response.data.token);
    }
    return response;
  },

  register: async (data: RegisterRequest) => {
    const response = await apiRequest<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    if (response.success && response.data.token) {
      setAuthToken(response.data.token);
    }
    return response;
  },

  logout: () => {
    setAuthToken(null);
  },

  getCurrentUser: () => apiRequest<User>('/auth/me'),
};

// Search API
export const searchApi = {
  getTrending: (limit = 10) => apiRequest<string[]>(`/search/trending?limit=${limit}`),

  getRecent: (limit = 10) => apiRequest<string[]>(`/search/recent?limit=${limit}`),

  clearHistory: () =>
    apiRequest<void>('/search/history', { method: 'DELETE' }),
};

// User API
export const userApi = {
  getProfile: () => apiRequest<User>('/users/profile'),

  updateProfile: (data: { firstName?: string; lastName?: string; avatarUrl?: string }) =>
    apiRequest<User>('/users/profile', {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
};

// Admin API
export const adminApi = {
  getStats: () => apiRequest<AdminStats>('/admin/stats'),
  runScraper: () => apiRequest<Record<string, unknown>>('/admin/scrape', { method: 'POST' }),
  ingest: (payload: Record<string, unknown>) =>
    apiRequest<Product>('/admin/ingest', { method: 'POST', body: JSON.stringify(payload) }),
};

// Faker API
export const fakerApi = {
  getStatus: () => apiRequest<FakerStatus>('/faker/status'),
  start: (intervalMs = 3000, batchSize = 5) =>
    apiRequest<Record<string, unknown>>(`/faker/start?intervalMs=${intervalMs}&batchSize=${batchSize}`, { method: 'POST' }),
  stop: () => apiRequest<void>('/faker/stop', { method: 'POST' }),
};

// Favorites API
export const favoritesApi = {
  getAll: () => apiRequest<Product[]>('/favorites'),

  getIds: () => apiRequest<string[]>('/favorites/ids'),

  add: (productId: string) =>
    apiRequest<void>(`/favorites/${productId}`, { method: 'POST' }),

  remove: (productId: string) =>
    apiRequest<void>(`/favorites/${productId}`, { method: 'DELETE' }),

  check: (productId: string) => apiRequest<boolean>(`/favorites/${productId}/check`),
};
