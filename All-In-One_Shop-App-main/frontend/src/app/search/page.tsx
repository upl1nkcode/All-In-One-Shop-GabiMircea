'use client';

import { useState, useEffect, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { Search, SlidersHorizontal, X, Loader2 } from 'lucide-react';
import { Header } from '@/components/Header';
import { ProductCard } from '@/components/ProductCard';
import type { Product, Brand, Category, Store } from '@/lib/types';
import { productApi, catalogApi, storeApi } from '@/lib/api';

function SearchContent() {
  const searchParams = useSearchParams();
  const q = searchParams.get('q') || '';
  const catParam = searchParams.get('category') || '';

  const [query, setQuery] = useState(q);
  const [products, setProducts] = useState<Product[]>([]);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [stores, setStores] = useState<Store[]>([]);
  const [loading, setLoading] = useState(true);
  const [showFilters, setShowFilters] = useState(false);

  // Filters
  const [selectedBrand, setSelectedBrand] = useState('');
  const [selectedCategory, setSelectedCategory] = useState(catParam);
  const [sortBy, setSortBy] = useState<string>('price_asc');

  useEffect(() => {
    const load = async () => {
      try {
        const [prodRes, brandRes, catRes, storeRes] = await Promise.all([
          productApi.getAll({ query: q || undefined, categoryId: catParam || undefined, sortBy: sortBy as any }),
          catalogApi.getBrands(),
          catalogApi.getCategories(),
          storeApi.getAll(),
        ]);
        setProducts(prodRes.data || []);
        setBrands(brandRes.data || []);
        setCategories(catRes.data || []);
        setStores(storeRes.data || []);
      } catch {
        setProducts([]);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [q, catParam, sortBy]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Search header */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              {q ? `Results for "${q}"` : 'All Products'}
            </h1>
            <p className="text-gray-500 mt-1">
              {loading ? 'Searching...' : `${products.length} products found`}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="btn-secondary text-sm py-2 px-4 flex items-center gap-2 lg:hidden"
            >
              <SlidersHorizontal className="w-4 h-4" /> Filters
            </button>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="input-field text-sm py-2 px-3 w-44"
            >
              <option value="price_asc">Price: Low → High</option>
              <option value="price_desc">Price: High → Low</option>
              <option value="name_asc">Name A–Z</option>
              <option value="newest">Newest</option>
            </select>
          </div>
        </div>

        <div className="flex gap-8">
          {/* Filter sidebar */}
          <aside className={`${showFilters ? 'block' : 'hidden'} lg:block w-64 shrink-0`}>
            <div className="bg-white rounded-xl p-6 sticky top-24 border border-gray-200">
              <h3 className="font-bold text-gray-900 mb-5">Filters</h3>

              {/* Category */}
              <div className="mb-6">
                <h4 className="font-semibold text-gray-700 mb-3 text-sm">Category</h4>
                <div className="space-y-2">
                  <label className="flex items-center gap-2 cursor-pointer text-sm">
                    <input type="radio" name="cat" checked={!selectedCategory} onChange={() => setSelectedCategory('')} className="text-brand-600" />
                    All
                  </label>
                  {categories.map(cat => (
                    <label key={cat.id} className="flex items-center gap-2 cursor-pointer text-sm">
                      <input type="radio" name="cat" checked={selectedCategory === cat.id} onChange={() => setSelectedCategory(cat.id)} className="text-brand-600" />
                      {cat.name}
                    </label>
                  ))}
                </div>
              </div>

              {/* Brand */}
              <div className="mb-6">
                <h4 className="font-semibold text-gray-700 mb-3 text-sm">Brand</h4>
                <div className="space-y-2">
                  {brands.map(brand => (
                    <label key={brand.id} className="flex items-center gap-2 cursor-pointer text-sm">
                      <input type="checkbox" checked={selectedBrand === brand.id} onChange={() => setSelectedBrand(selectedBrand === brand.id ? '' : brand.id)} className="rounded text-brand-600" />
                      {brand.name}
                    </label>
                  ))}
                </div>
              </div>

              <button onClick={() => { setSelectedBrand(''); setSelectedCategory(''); }} className="w-full btn-secondary text-sm py-2">
                Clear Filters
              </button>
            </div>
          </aside>

          {/* Product grid */}
          <div className="flex-1">
            {loading ? (
              <div className="flex items-center justify-center py-20">
                <Loader2 className="w-8 h-8 animate-spin text-brand-600" />
              </div>
            ) : products.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map(product => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
            ) : (
              <div className="text-center py-20 bg-white rounded-xl border border-gray-200">
                <Search className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <p className="text-lg text-gray-500">No products found</p>
                <p className="text-sm text-gray-400 mt-2">Try adjusting your search or filters</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center"><Loader2 className="w-8 h-8 animate-spin text-brand-600" /></div>}>
      <SearchContent />
    </Suspense>
  );
}
