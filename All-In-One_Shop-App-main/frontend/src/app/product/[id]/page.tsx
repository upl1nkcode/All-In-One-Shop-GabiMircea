'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ArrowLeft, ExternalLink, Share2, Loader2, ShoppingBag } from 'lucide-react';
import { Header } from '@/components/Header';
import { productApi } from '@/lib/api';
import type { Product } from '@/lib/types';

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    productApi.getById(id)
      .then(res => setProduct(res.data))
      .catch(() => setProduct(null))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-brand-600" />
      </div>
    );
  }

  if (!product) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Product not found</h2>
        <button onClick={() => router.push('/')} className="btn-primary">Go Home</button>
      </div>
    );
  }

  const lowestPrice = product.lowestPrice ?? Math.min(...product.prices.map(p => p.price));
  const sortedPrices = [...product.prices].sort((a, b) => a.price - b.price);

  const handleShare = async () => {
    if (navigator.share) {
      await navigator.share({ title: product.name, url: window.location.href });
    } else {
      await navigator.clipboard.writeText(window.location.href);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Breadcrumb */}
        <button onClick={() => router.back()} className="flex items-center gap-2 text-gray-500 hover:text-gray-700 mb-6 transition-colors">
          <ArrowLeft className="w-4 h-4" /> Back to results
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
          {/* Image */}
          <div className="bg-white rounded-2xl overflow-hidden border border-gray-200">
            <div className="aspect-square">
              <img
                src={product.imageUrl || '/placeholder.jpg'}
                alt={product.name}
                className="w-full h-full object-cover"
              />
            </div>
            {product.additionalImages && product.additionalImages.length > 0 && (
              <div className="flex gap-2 p-4 overflow-x-auto">
                {product.additionalImages.map((img, i) => (
                  <img key={i} src={img} alt="" className="w-20 h-20 object-cover rounded-lg border-2 border-transparent hover:border-brand-500 cursor-pointer" />
                ))}
              </div>
            )}
          </div>

          {/* Info */}
          <div className="space-y-6">
            <div>
              {product.brand && (
                <span className="text-sm font-semibold text-brand-600 uppercase tracking-wider">
                  {product.brand.name}
                </span>
              )}
              <h1 className="text-3xl font-bold text-gray-900 mt-1">{product.name}</h1>
              {product.category && (
                <p className="text-gray-500 mt-1">{product.category.name}</p>
              )}
            </div>

            {/* Price highlight */}
            <div className="bg-gradient-to-br from-brand-50 to-purple-50 rounded-2xl p-6 border border-brand-100">
              <div className="flex items-baseline gap-3">
                <span className="text-sm text-gray-600">Best price:</span>
                <span className="text-4xl font-bold text-gray-900">
                  {sortedPrices[0]?.currency || '€'}{lowestPrice}
                </span>
              </div>
              <p className="text-sm text-gray-500 mt-2">
                Available at {product.storeCount || product.prices.length} stores
              </p>
            </div>

            {/* Description */}
            {product.description && (
              <div>
                <h3 className="font-semibold text-gray-900 mb-2">Description</h3>
                <p className="text-gray-600 leading-relaxed">{product.description}</p>
              </div>
            )}

            {/* Details */}
            <div className="bg-white rounded-xl p-5 border border-gray-200 space-y-3">
              {product.brand && (
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Brand</span>
                  <span className="font-medium text-gray-900">{product.brand.name}</span>
                </div>
              )}
              {product.sizes && product.sizes.length > 0 && (
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Sizes</span>
                  <span className="font-medium text-gray-900">{product.sizes.join(', ')}</span>
                </div>
              )}
              {product.colors && product.colors.length > 0 && (
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Colors</span>
                  <span className="font-medium text-gray-900">{product.colors.join(', ')}</span>
                </div>
              )}
              {product.gender && (
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Gender</span>
                  <span className="font-medium text-gray-900 capitalize">{product.gender.toLowerCase()}</span>
                </div>
              )}
            </div>

            <button onClick={handleShare} className="btn-secondary flex items-center gap-2">
              <Share2 className="w-4 h-4" /> Share
            </button>
          </div>
        </div>

        {/* Where to Buy */}
        <div className="mt-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Where to Buy</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {sortedPrices.map((price) => {
              const isLowest = price.price === lowestPrice;
              return (
                <div
                  key={price.id}
                  className={`card p-6 ${isLowest ? 'ring-2 ring-green-500 border-green-500' : ''}`}
                >
                  {isLowest && (
                    <span className="inline-block bg-green-500 text-white text-xs font-bold px-2.5 py-1 rounded-lg mb-3">
                      Best Price
                    </span>
                  )}
                  <div className="flex items-center justify-between mb-4">
                    <div>
                      <h3 className="font-bold text-gray-900">{price.store.name}</h3>
                      <p className="text-xs text-gray-500">{price.store.website}</p>
                    </div>
                  </div>
                  <div className="flex items-baseline gap-2 mb-4">
                    <span className="text-3xl font-bold text-gray-900">{price.currency}{price.price}</span>
                    {price.originalPrice && price.originalPrice > price.price && (
                      <span className="text-sm text-gray-400 line-through">{price.currency}{price.originalPrice}</span>
                    )}
                    {!isLowest && (
                      <span className="text-sm text-red-500 ml-auto">+{price.currency}{(price.price - lowestPrice).toFixed(2)}</span>
                    )}
                  </div>
                  <span className={`text-xs font-medium ${price.inStock ? 'text-green-600' : 'text-red-500'}`}>
                    {price.inStock ? '● In Stock' : '● Out of Stock'}
                  </span>
                  <a
                    href={price.productUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn-primary w-full mt-4 flex items-center justify-center gap-2 text-sm"
                  >
                    Buy at {price.store.name} <ExternalLink className="w-4 h-4" />
                  </a>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
