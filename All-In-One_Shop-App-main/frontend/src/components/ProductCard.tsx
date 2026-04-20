import Link from 'next/link';
import type { Product } from '@/lib/types';

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  const prices = product.prices || [];
  const lowestPrice = product.lowestPrice ?? (prices.length > 0 ? Math.min(...prices.map(p => p.price)) : 0);
  const lowestEntry = prices.find(p => p.price === lowestPrice);
  const currency = lowestEntry?.currency || '€';

  return (
    <Link href={`/product/${product.id}`} className="card group overflow-hidden">
      <div className="aspect-square overflow-hidden bg-gray-100 relative">
        <img
          src={product.imageUrl || '/placeholder.jpg'}
          alt={product.name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
        />
        {product.prices?.some(p => p.originalPrice && p.originalPrice > p.price) && (
          <span className="absolute top-3 left-3 bg-red-500 text-white text-xs font-bold px-2.5 py-1 rounded-lg">
            SALE
          </span>
        )}
      </div>
      <div className="p-4">
        {product.brand && (
          <span className="text-xs font-semibold text-brand-600 uppercase tracking-wider">
            {product.brand.name}
          </span>
        )}
        <h3 className="mt-1 font-semibold text-gray-900 group-hover:text-brand-600 transition-colors line-clamp-2">
          {product.name}
        </h3>
        <div className="mt-2 flex items-baseline gap-2">
          <span className="text-xl font-bold text-gray-900">{currency}{lowestPrice}</span>
          {lowestEntry?.originalPrice && lowestEntry.originalPrice > lowestPrice && (
            <span className="text-sm text-gray-400 line-through">{currency}{lowestEntry.originalPrice}</span>
          )}
        </div>
        <p className="mt-1.5 text-sm text-gray-500">
          {(product.storeCount || prices.length)} {(product.storeCount || prices.length) === 1 ? 'store' : 'stores'}
        </p>
      </div>
    </Link>
  );
}
