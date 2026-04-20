import { Heart } from 'lucide-react';
import { useNavigate } from 'react-router';
import { useState } from 'react';
import type { Product } from '../api/types';
import { Card, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { useAuth } from '../context/AuthContext';
import { favoritesApi } from '../api/client';
import { useFavoriteIds } from '../api/hooks';
import { toast } from 'sonner';
import { mutate } from 'swr';

interface ProductCardProps {
  product: Product;
  showFavoriteButton?: boolean;
}

export function ProductCard({ product, showFavoriteButton = true }: ProductCardProps) {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { data: favoriteIds } = useFavoriteIds();
  const [isToggling, setIsToggling] = useState(false);

  // Find the lowest price - use lowestPrice if available, otherwise calculate
  const prices = product.prices || [];
  const lowestPrice = product.lowestPrice ?? (prices.length > 0 ? Math.min(...prices.map((p) => p.price)) : 0);
  const lowestPriceEntry = prices.find((p) => p.price === lowestPrice);
  const currency = lowestPriceEntry?.currency || '€';

  const isFavorite = favoriteIds?.includes(product.id) ?? false;

  const handleFavoriteClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    
    if (!isAuthenticated) {
      toast.error('Please sign in to save favorites');
      return;
    }

    setIsToggling(true);
    try {
      if (isFavorite) {
        await favoritesApi.remove(product.id);
        toast.success('Removed from favorites');
      } else {
        await favoritesApi.add(product.id);
        toast.success('Added to favorites');
      }
      // Revalidate favorites data
      mutate('favorite-ids');
      mutate('favorites');
    } catch (error) {
      toast.error('Failed to update favorites');
    } finally {
      setIsToggling(false);
    }
  };

  return (
    <Card
      className="overflow-hidden hover:shadow-xl transition-shadow cursor-pointer relative group"
      onClick={() => navigate(`/product/${product.id}`)}
    >
      {showFavoriteButton && (
        <Button
          variant="ghost"
          size="icon"
          className={`absolute top-2 right-2 z-10 bg-white/80 hover:bg-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity ${
            isFavorite ? 'opacity-100' : ''
          }`}
          onClick={handleFavoriteClick}
          disabled={isToggling}
        >
          <Heart
            className={`h-5 w-5 transition-colors ${
              isFavorite ? 'fill-red-500 text-red-500' : 'text-slate-600'
            }`}
          />
        </Button>
      )}
      <div className="aspect-square overflow-hidden bg-slate-100">
        <img
          src={product.imageUrl || '/placeholder.jpg'}
          alt={product.name}
          className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
        />
      </div>
      <CardContent className="p-4">
        {product.brand && (
          <Badge variant="secondary" className="mb-2">
            {product.brand.name}
          </Badge>
        )}
        <h3 className="font-semibold text-lg mb-2 text-slate-900 line-clamp-2">
          {product.name}
        </h3>
        <div className="flex items-center justify-between mb-3">
          <div>
            <div className="text-2xl font-bold text-slate-900">
              {currency}{lowestPrice}
            </div>
            <div className="text-sm text-slate-600">
              {(product.storeCount || prices.length) > 1 
                ? `Lowest of ${product.storeCount || prices.length} stores`
                : 'Lowest price'
              }
            </div>
          </div>
        </div>
        {prices.length > 0 && (
          <div className="space-y-1">
            <div className="text-sm font-medium text-slate-700">Available at:</div>
            {prices.slice(0, 3).map((price, index) => (
              <div
                key={price.id || `price-${index}`}
                className="flex items-center justify-between text-sm"
              >
                <span className="text-slate-600">{price.store?.name || 'Store'}</span>
                <span className={`font-semibold ${price.price === lowestPrice ? 'text-green-600' : 'text-slate-900'}`}>
                  {price.currency || '€'}{price.price}
                </span>
              </div>
            ))}
            {prices.length > 3 && (
              <div className="text-sm text-blue-600">
                +{prices.length - 3} more stores
              </div>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
