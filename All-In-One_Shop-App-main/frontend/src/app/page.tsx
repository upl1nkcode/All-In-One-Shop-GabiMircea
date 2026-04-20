'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { Search, TrendingUp, Zap, DollarSign, ArrowRight, ShoppingBag } from 'lucide-react';
import { Header } from '@/components/Header';
import { Footer } from '@/components/Footer';
import { productApi } from '@/lib/api';

const categories = [
  { name: 'Sneakers', slug: 'sneakers', icon: '👟', count: 120 },
  { name: 'Hoodies', slug: 'hoodies', icon: '🧥', count: 85 },
  { name: 'Jackets', slug: 'jackets', icon: '🧥', count: 64 },
  { name: 'Pants', slug: 'pants', icon: '👖', count: 92 },
  { name: 'T-Shirts', slug: 't-shirts', icon: '👕', count: 156 },
  { name: 'Jeans', slug: 'jeans', icon: '👖', count: 78 },
];

const stores = ['Zalando', 'Nike', 'ASOS', 'H&M', 'AboutYou'];

export default function LandingPage() {
  const [trendingProducts, setTrendingProducts] = useState<any[]>([]);
  const [loadingTrending, setLoadingTrending] = useState(true);

  useEffect(() => {
    productApi.getTrending(6)
      .then((res) => {
        setTrendingProducts(res.data || []);
      })
      .catch(() => {})
      .finally(() => setLoadingTrending(false));
  }, []);

  return (
    <div className="min-h-screen bg-white">
      <Header />

      {/* Hero */}
      <section className="relative overflow-hidden bg-gradient-to-br from-brand-50 via-white to-purple-50">
        <div className="absolute inset-0 bg-[url('/grid.svg')] opacity-5" />
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-32">
          <div className="max-w-3xl mx-auto text-center">
            <div className="inline-flex items-center gap-2 bg-brand-100 text-brand-700 px-4 py-1.5 rounded-full text-sm font-medium mb-6">
              <ShoppingBag className="w-4 h-4" />
              Compare prices from {stores.length}+ stores
            </div>
            <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold text-gray-900 tracking-tight">
              Find the{' '}
              <span className="bg-gradient-to-r from-brand-600 to-purple-600 bg-clip-text text-transparent">
                Best Deals
              </span>{' '}
              on Fashion
            </h1>
            <p className="mt-6 text-xl text-gray-600 max-w-2xl mx-auto">
              One search. Every store. Always the lowest price. Compare products from Zalando, Nike, ASOS, and more.
            </p>

            {/* Search Bar */}
            <form action="/search" className="mt-10 max-w-2xl mx-auto">
              <div className="relative flex items-center">
                <Search className="absolute left-5 text-gray-400 w-5 h-5" />
                <input
                  type="text"
                  name="q"
                  placeholder="Search for products, brands, or categories..."
                  className="w-full pl-14 pr-36 py-4 text-lg rounded-2xl border-2 border-gray-200 focus:border-brand-500 focus:ring-4 focus:ring-brand-100 shadow-lg shadow-gray-100/50 transition-all"
                />
                <button
                  type="submit"
                  className="absolute right-2 btn-primary rounded-xl px-8 py-3 text-base"
                >
                  Search
                </button>
              </div>
            </form>

            {/* Store logos */}
            <div className="mt-12 flex items-center justify-center gap-8 text-gray-400">
              <span className="text-sm font-medium">Searching across:</span>
              {stores.map((store) => (
                <span key={store} className="text-sm font-semibold text-gray-500">
                  {store}
                </span>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Trending Products */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="flex items-center justify-between mb-10">
          <div>
            <h2 className="text-3xl font-bold text-gray-900">Trending Now</h2>
            <p className="mt-2 text-gray-600">Most popular products across all stores</p>
          </div>
          <Link href="/search" className="text-brand-600 hover:text-brand-700 font-medium flex items-center gap-1">
            View all <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        {loadingTrending ? (
          <div className="flex justify-center py-16">
            <span className="w-8 h-8 border-3 border-brand-600 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : trendingProducts.length === 0 ? (
          <div className="text-center py-16 text-gray-500">
            <p className="text-lg">No products yet. Run the scraper from the admin dashboard to populate data.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {trendingProducts.map((product: any) => (
              <Link key={product.id} href={`/product/${product.id}`} className="card group overflow-hidden">
                <div className="aspect-square overflow-hidden bg-gray-100">
                  <img
                    src={product.imageUrl || '/placeholder.png'}
                    alt={product.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  />
                </div>
                <div className="p-5">
                  <span className="text-xs font-semibold text-brand-600 uppercase tracking-wider">
                    {product.brand?.name || ''}
                  </span>
                  <h3 className="mt-1 text-lg font-semibold text-gray-900 group-hover:text-brand-600 transition-colors line-clamp-2">
                    {product.name}
                  </h3>
                  <div className="mt-3 flex items-baseline gap-2">
                    <span className="text-2xl font-bold text-gray-900">
                      {product.lowestPrice != null ? `€${product.lowestPrice}` : 'N/A'}
                    </span>
                    {product.highestPrice != null && product.highestPrice !== product.lowestPrice && (
                      <span className="text-sm text-gray-400 line-through">€{product.highestPrice}</span>
                    )}
                  </div>
                  <p className="mt-2 text-sm text-gray-500">
                    Available at {product.storeCount || product.prices?.length || 0} store{(product.storeCount || product.prices?.length || 0) !== 1 ? 's' : ''}
                  </p>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>

      {/* Categories */}
      <section className="bg-gray-50 py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-gray-900 text-center mb-12">Shop by Category</h2>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {categories.map((cat) => (
              <Link
                key={cat.slug}
                href={`/search?category=${cat.slug}`}
                className="bg-white rounded-2xl p-6 text-center hover:shadow-lg hover:border-brand-300 border-2 border-transparent transition-all group"
              >
                <div className="text-4xl mb-3">{cat.icon}</div>
                <div className="font-semibold text-gray-900 group-hover:text-brand-600 transition-colors">
                  {cat.name}
                </div>
                <div className="text-sm text-gray-400 mt-1">{cat.count}+ items</div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <h2 className="text-3xl font-bold text-gray-900 text-center mb-12">Why AllInOne Shop?</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
          <div className="text-center">
            <div className="w-14 h-14 bg-brand-100 rounded-2xl flex items-center justify-center mx-auto mb-5">
              <TrendingUp className="w-7 h-7 text-brand-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Compare Prices</h3>
            <p className="text-gray-600">See prices from all major stores side by side and find the best deal instantly.</p>
          </div>
          <div className="text-center">
            <div className="w-14 h-14 bg-green-100 rounded-2xl flex items-center justify-center mx-auto mb-5">
              <Zap className="w-7 h-7 text-green-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Fast & Easy</h3>
            <p className="text-gray-600">One search shows results from everywhere. Click through to buy directly at the store.</p>
          </div>
          <div className="text-center">
            <div className="w-14 h-14 bg-amber-100 rounded-2xl flex items-center justify-center mx-auto mb-5">
              <DollarSign className="w-7 h-7 text-amber-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Save Money</h3>
            <p className="text-gray-600">Always find the lowest price. We track discounts and price drops for you.</p>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="bg-gradient-to-r from-brand-600 to-purple-600 py-16">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
            Start saving on fashion today
          </h2>
          <p className="text-lg text-brand-100 mb-8">
            Join thousands of smart shoppers who compare before they buy.
          </p>
          <Link
            href="/search"
            className="inline-flex items-center gap-2 bg-white text-brand-700 font-semibold px-8 py-3.5 rounded-xl hover:bg-brand-50 transition-colors text-lg"
          >
            Browse Products <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>

      <Footer />
    </div>
  );
}
