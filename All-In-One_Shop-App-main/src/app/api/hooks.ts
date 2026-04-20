// SWR hooks for data fetching
import useSWR from 'swr';
import useSWRMutation from 'swr/mutation';
import { productApi, catalogApi, favoritesApi, authApi, searchApi, userApi } from './client';
import type { SearchRequest, Product, Category, Brand, Store, UpdateProfileRequest } from './types';
import { products as mockProducts, stores as mockStores, categories as mockCategories, brands as mockBrands } from '../data/mockData';

// Helper to transform mock data to API format
function transformMockProduct(mockProduct: typeof mockProducts[0]): Product {
  const store = mockStores.find(s => s.id === mockProduct.prices[0]?.storeId);
  return {
    id: mockProduct.id,
    name: mockProduct.name,
    description: mockProduct.description,
    imageUrl: mockProduct.imageUrl,
    brand: { id: mockProduct.brand, name: mockProduct.brand },
    category: { id: mockProduct.category, name: mockProduct.category, slug: mockProduct.category.toLowerCase() },
    prices: mockProduct.prices.map((p, idx) => {
      const priceStore = mockStores.find(s => s.id === p.storeId);
      return {
        id: `${mockProduct.id}-${idx}`,
        store: priceStore ? { id: priceStore.id, name: priceStore.name, website: priceStore.website } : { id: p.storeId, name: 'Store', website: '' },
        price: p.price,
        currency: p.currency,
        productUrl: p.productUrl,
        inStock: true,
      };
    }),
    storeCount: mockProduct.prices.length,
    lowestPrice: Math.min(...mockProduct.prices.map(p => p.price)),
  };
}

const transformedMockProducts = mockProducts.map(transformMockProduct);

// SWR options with error handling for when backend isn't available
const swrOptions = {
  onErrorRetry: (error: Error, key: string, config: unknown, revalidate: unknown, { retryCount }: { retryCount: number }) => {
    // Don't retry on certain errors
    if (retryCount >= 1) return;
  },
  shouldRetryOnError: false,
};

// Product hooks with fallback to mock data
export function useProducts() {
  return useSWR('products', async () => {
    try {
      const response = await productApi.getAll();
      return response.data;
    } catch {
      return transformedMockProducts;
    }
  }, swrOptions);
}

export function useProduct(id: string | undefined) {
  return useSWR(
    id ? `product-${id}` : null,
    async () => {
      try {
        const response = await productApi.getById(id!);
        return response.data;
      } catch {
        return transformedMockProducts.find(p => p.id === id);
      }
    },
    swrOptions
  );
}

export function useProductSearch(request: SearchRequest) {
  const key = JSON.stringify({ search: request });
  return useSWR(key, async () => {
    try {
      const response = await productApi.search(request);
      return response.data;
    } catch {
      // Filter mock data based on search request
      let results = [...transformedMockProducts];
      if (request.query) {
        const q = request.query.toLowerCase();
        results = results.filter(p => 
          p.name.toLowerCase().includes(q) || 
          p.brand?.name.toLowerCase().includes(q) ||
          p.category?.name.toLowerCase().includes(q)
        );
      }
      if (request.minPrice) {
        results = results.filter(p => (p.lowestPrice || 0) >= request.minPrice!);
      }
      if (request.maxPrice) {
        results = results.filter(p => (p.lowestPrice || 0) <= request.maxPrice!);
      }
      return results;
    }
  }, swrOptions);
}

export function useSimilarProducts(productId: string | undefined, limit = 4) {
  return useSWR(
    productId ? `similar-${productId}` : null,
    async () => {
      try {
        const response = await productApi.getSimilar(productId!, limit);
        return response.data;
      } catch {
        const current = transformedMockProducts.find(p => p.id === productId);
        return transformedMockProducts
          .filter(p => p.id !== productId && p.category?.name === current?.category?.name)
          .slice(0, limit);
      }
    },
    swrOptions
  );
}

export function useTrendingProducts(limit = 8) {
  return useSWR('trending', async () => {
    try {
      const response = await productApi.getTrending(limit);
      return response.data;
    } catch {
      return transformedMockProducts.slice(0, limit);
    }
  }, swrOptions);
}

// Catalog hooks with fallback
export function useCategories() {
  return useSWR('categories', async () => {
    try {
      const response = await catalogApi.getCategories();
      return response.data;
    } catch {
      return mockCategories.map((name, idx) => ({ 
        id: String(idx), 
        name, 
        slug: name.toLowerCase().replace(/\s+/g, '-') 
      }));
    }
  }, swrOptions);
}

export function useBrands() {
  return useSWR('brands', async () => {
    try {
      const response = await catalogApi.getBrands();
      return response.data;
    } catch {
      return mockBrands.map((name, idx) => ({ id: String(idx), name }));
    }
  }, swrOptions);
}

export function useStores() {
  return useSWR('stores', async () => {
    try {
      const response = await catalogApi.getStores();
      return response.data;
    } catch {
      return mockStores;
    }
  }, swrOptions);
}

// Favorites hooks with fallback
export function useFavorites() {
  return useSWR('favorites', async () => {
    try {
      const response = await favoritesApi.getAll();
      return response.data;
    } catch {
      return [];
    }
  }, swrOptions);
}

export function useFavoriteIds() {
  return useSWR('favorite-ids', async () => {
    try {
      const response = await favoritesApi.getIds();
      return response.data;
    } catch {
      return [];
    }
  }, swrOptions);
}

export function useAddFavorite() {
  return useSWRMutation(
    'favorites',
    (_, { arg }: { arg: string }) => favoritesApi.add(arg)
  );
}

export function useRemoveFavorite() {
  return useSWRMutation(
    'favorites',
    (_, { arg }: { arg: string }) => favoritesApi.remove(arg)
  );
}

// Auth hooks
export function useCurrentUser() {
  return useSWR('current-user', async () => {
    try {
      const response = await authApi.getCurrentUser();
      return response.data;
    } catch {
      return null;
    }
  }, {
    ...swrOptions,
    revalidateOnFocus: false,
  });
}

// Search history hooks
export function useTrendingSearches(limit = 10) {
  return useSWR('trending-searches', async () => {
    try {
      const response = await searchApi.getTrending(limit);
      return response.data;
    } catch {
      return [];
    }
  }, swrOptions);
}

export function useRecentSearches(limit = 10) {
  return useSWR('recent-searches', async () => {
    try {
      const response = await searchApi.getRecent(limit);
      return response.data;
    } catch {
      return [];
    }
  }, swrOptions);
}

export function useClearSearchHistory() {
  return useSWRMutation(
    'recent-searches',
    () => searchApi.clearHistory()
  );
}

// User profile hooks
export function useUserProfile() {
  return useSWR('user-profile', async () => {
    try {
      const response = await userApi.getProfile();
      return response.data;
    } catch {
      return null;
    }
  }, {
    ...swrOptions,
    revalidateOnFocus: false,
  });
}

export function useUpdateProfile() {
  return useSWRMutation(
    'user-profile',
    (_, { arg }: { arg: UpdateProfileRequest }) => userApi.updateProfile(arg)
  );
}
