// API client for the Spring Boot backend
// ================================================
import type {
  ApiResponse, Product, Brand, Category, Store,
  AuthResponse, SearchParams, PagedResponse, DashboardStats,
} from './types';

const API_URL = '/api';

// ── Token management ─────────────────────────────────────────
function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('auth_token');
}

export function setToken(token: string | null) {
  if (typeof window === 'undefined') return;
  if (token) {
    localStorage.setItem('auth_token', token);
  } else {
    localStorage.removeItem('auth_token');
  }
}

// ── Generic fetch wrapper ────────────────────────────────────
async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Request failed' }));
    throw new Error(err.message || `HTTP ${res.status}`);
  }

  const text = await res.text();
  if (!text) return { success: true, data: null } as T;
  return JSON.parse(text) as T;
}

// ── Products ─────────────────────────────────────────────────
export const productApi = {
  getAll: (params?: SearchParams) => {
    const qs = new URLSearchParams();
    if (params?.query) qs.set('query', params.query);
    if (params?.brandId) qs.set('brandId', params.brandId);
    if (params?.categoryId) qs.set('categoryId', params.categoryId);
    if (params?.minPrice) qs.set('minPrice', String(params.minPrice));
    if (params?.maxPrice) qs.set('maxPrice', String(params.maxPrice));
    if (params?.gender) qs.set('gender', params.gender);
    if (params?.sortBy) qs.set('sortBy', params.sortBy);
    if (params?.page) qs.set('page', String(params.page));
    if (params?.size) qs.set('size', String(params.size));
    const q = qs.toString();
    return apiFetch<ApiResponse<Product[]>>(`/products${q ? `?${q}` : ''}`);
  },

  getById: (id: string) =>
    apiFetch<ApiResponse<Product>>(`/products/${id}`),

  create: (data: Partial<Product>) =>
    apiFetch<ApiResponse<Product>>('/products', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  update: (id: string, data: Partial<Product>) =>
    apiFetch<ApiResponse<Product>>(`/products/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  delete: (id: string) =>
    apiFetch<ApiResponse<void>>(`/products/${id}`, { method: 'DELETE' }),

  getTrending: (limit = 8) =>
    apiFetch<ApiResponse<Product[]>>(`/products/trending?limit=${limit}`),
};

// ── Stores ───────────────────────────────────────────────────
export const storeApi = {
  getAll: () => apiFetch<ApiResponse<Store[]>>('/stores'),
  create: (data: Partial<Store>) =>
    apiFetch<ApiResponse<Store>>('/stores', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: string, data: Partial<Store>) =>
    apiFetch<ApiResponse<Store>>(`/stores/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
  delete: (id: string) =>
    apiFetch<ApiResponse<void>>(`/stores/${id}`, { method: 'DELETE' }),
};

// ── Catalog ──────────────────────────────────────────────────
export const catalogApi = {
  getCategories: () => apiFetch<ApiResponse<Category[]>>('/categories'),
  getBrands: () => apiFetch<ApiResponse<Brand[]>>('/brands'),
};

// ── Auth ─────────────────────────────────────────────────────
export const authApi = {
  login: async (email: string, password: string) => {
    const res = await apiFetch<ApiResponse<AuthResponse>>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    if (res.success && res.data.token) {
      setToken(res.data.token);
    }
    return res;
  },

  logout: () => {
    setToken(null);
  },

  me: () => apiFetch<ApiResponse<{ id: string; email: string; role: string }>>('/auth/me'),
};

// ── Admin ────────────────────────────────────────────────────
export const adminApi = {
  getStats: () => apiFetch<ApiResponse<DashboardStats>>('/admin/stats'),
  runScraper: () =>
    apiFetch<ApiResponse<{ totalProducts: number; totalIngested: number; totalStores: number; totalPrices: number }>>('/admin/scrape', {
      method: 'POST',
    }),
};
