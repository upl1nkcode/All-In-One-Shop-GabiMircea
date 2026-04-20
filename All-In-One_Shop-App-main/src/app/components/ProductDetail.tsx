import { ExternalLink, ArrowLeft, Heart, Share2, Loader2 } from 'lucide-react';
import { useNavigate, useParams } from 'react-router';
import { useState } from 'react';
import { useProduct, useSimilarProducts, useFavoriteIds } from '../api/hooks';
import { favoritesApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Card, CardContent } from './ui/card';
import { toast } from 'sonner';
import { mutate } from 'swr';

export function ProductDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { data: product, isLoading, error } = useProduct(id);
  const { data: similarProducts } = useSimilarProducts(id, 4);
  const { isAuthenticated } = useAuth();
  const { data: favoriteIds } = useFavoriteIds();
  const [isToggling, setIsToggling] = useState(false);

  const isFavorite = favoriteIds?.includes(id || '') ?? false;

  const handleFavoriteClick = async () => {
    if (!isAuthenticated) {
      toast.error('Please sign in to save favorites');
      return;
    }

    if (!id) return;

    setIsToggling(true);
    try {
      if (isFavorite) {
        await favoritesApi.remove(id);
        toast.success('Removed from favorites');
      } else {
        await favoritesApi.add(id);
        toast.success('Added to favorites');
      }
      mutate('favorite-ids');
      mutate('favorites');
    } catch (error) {
      toast.error('Failed to update favorites');
    } finally {
      setIsToggling(false);
    }
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: product?.name,
          text: `Check out ${product?.name} on AllInOne Shop!`,
          url: window.location.href,
        });
      } catch (err) {
        // User cancelled share
      }
    } else {
      await navigator.clipboard.writeText(window.location.href);
      toast.success('Link copied to clipboard!');
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-4 text-slate-900">Product not found</h2>
          <Button onClick={() => navigate('/')}>Go back home</Button>
        </div>
      </div>
    );
  }

  const lowestPrice = product.lowestPrice ?? Math.min(...product.prices.map((p) => p.price));
  const sortedPrices = [...product.prices].sort((a, b) => a.price - b.price);

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <header className="bg-white border-b sticky top-0 z-10">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button
                variant="ghost"
                size="icon"
                onClick={() => navigate(-1)}
              >
                <ArrowLeft size={20} />
              </Button>
              <h1
                className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent cursor-pointer"
                onClick={() => navigate('/')}
              >
                AllInOne Shop
              </h1>
            </div>
            <div className="flex gap-2">
              <Button 
                variant="ghost" 
                size="icon"
                onClick={handleFavoriteClick}
                disabled={isToggling}
              >
                <Heart 
                  size={20} 
                  className={isFavorite ? 'fill-red-500 text-red-500' : ''} 
                />
              </Button>
              <Button variant="ghost" size="icon" onClick={handleShare}>
                <Share2 size={20} />
              </Button>
            </div>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Product Image */}
          <div className="bg-white rounded-lg overflow-hidden">
            <div className="aspect-square">
              <img
                src={product.imageUrl || '/placeholder.jpg'}
                alt={product.name}
                className="w-full h-full object-cover"
              />
            </div>
            {/* Additional Images */}
            {product.additionalImages && product.additionalImages.length > 0 && (
              <div className="flex gap-2 p-4 overflow-x-auto">
                {product.additionalImages.map((img, index) => (
                  <img
                    key={index}
                    src={img}
                    alt={`${product.name} ${index + 1}`}
                    className="w-20 h-20 object-cover rounded-md cursor-pointer hover:ring-2 hover:ring-blue-500"
                  />
                ))}
              </div>
            )}
          </div>

          {/* Product Info */}
          <div className="space-y-6">
            <div>
              {product.brand && (
                <Badge variant="secondary" className="mb-2">
                  {product.brand.name}
                </Badge>
              )}
              <h1 className="text-3xl font-bold mb-2 text-slate-900">{product.name}</h1>
              {product.category && (
                <p className="text-slate-600">{product.category.name}</p>
              )}
            </div>

            {/* Price Info */}
            <Card className="bg-gradient-to-br from-blue-50 to-purple-50 border-blue-200">
              <CardContent className="p-6">
                <div className="flex items-baseline gap-2 mb-2">
                  <span className="text-sm text-slate-600">Lowest price:</span>
                  <span className="text-4xl font-bold text-slate-900">
                    {sortedPrices[0]?.currency || '€'}{lowestPrice}
                  </span>
                </div>
                {product.highestPrice && product.highestPrice !== lowestPrice && (
                  <p className="text-sm text-slate-600">
                    Save up to {sortedPrices[0]?.currency || '€'}{(product.highestPrice - lowestPrice).toFixed(2)} compared to other stores
                  </p>
                )}
                <p className="text-sm text-slate-600 mt-1">
                  Available at {product.storeCount || product.prices.length} stores
                </p>
              </CardContent>
            </Card>

            {/* Description */}
            {product.description && (
              <div>
                <h3 className="font-bold text-lg mb-2 text-slate-900">Description</h3>
                <p className="text-slate-600 leading-relaxed">{product.description}</p>
              </div>
            )}

            {/* Product Details */}
            <div className="bg-white rounded-lg p-4 space-y-2">
              {product.brand && (
                <div className="flex justify-between">
                  <span className="text-slate-600">Brand</span>
                  <span className="font-semibold text-slate-900">{product.brand.name}</span>
                </div>
              )}
              {product.category && (
                <div className="flex justify-between">
                  <span className="text-slate-600">Category</span>
                  <span className="font-semibold text-slate-900">{product.category.name}</span>
                </div>
              )}
              {product.sizes && product.sizes.length > 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-600">Available Sizes</span>
                  <span className="font-semibold text-slate-900">{product.sizes.join(', ')}</span>
                </div>
              )}
              {product.colors && product.colors.length > 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-600">Colors</span>
                  <span className="font-semibold text-slate-900">{product.colors.join(', ')}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-slate-600">Availability</span>
                <span className="font-semibold text-green-600">In Stock</span>
              </div>
            </div>
          </div>
        </div>

        {/* Where to Buy */}
        <div className="bg-white rounded-lg p-6">
          <h2 className="text-2xl font-bold mb-6 text-slate-900">Where to Buy</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {sortedPrices.map((price) => {
              const isLowest = price.price === lowestPrice;
              return (
                <Card
                  key={price.id}
                  className={`hover:shadow-lg transition-shadow ${
                    isLowest ? 'border-2 border-green-500' : ''
                  }`}
                >
                  <CardContent className="p-6">
                    {isLowest && (
                      <Badge className="mb-2 bg-green-500">Best Price</Badge>
                    )}
                    {price.savings && price.savings > 0 && (
                      <Badge variant="destructive" className="mb-2 ml-2">
                        Save {price.currency}{price.savings.toFixed(2)}
                      </Badge>
                    )}
                    <div className="flex items-center justify-between mb-4">
                      <div>
                        <h3 className="font-bold text-lg text-slate-900">{price.store.name}</h3>
                        <p className="text-sm text-slate-600">{price.store.website}</p>
                      </div>
                      {price.store.logoUrl && (
                        <img 
                          src={price.store.logoUrl} 
                          alt={price.store.name}
                          className="h-8 w-auto object-contain"
                        />
                      )}
                    </div>
                    <div className="flex items-baseline justify-between mb-4">
                      <div>
                        <span className="text-3xl font-bold text-slate-900">
                          {price.currency}{price.price}
                        </span>
                        {price.originalPrice && price.originalPrice > price.price && (
                          <span className="ml-2 text-lg text-slate-400 line-through">
                            {price.currency}{price.originalPrice}
                          </span>
                        )}
                      </div>
                      {!isLowest && (
                        <span className="text-sm text-red-600">
                          +{price.currency}{(price.price - lowestPrice).toFixed(2)}
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-2 mb-4">
                      <span className={`text-sm ${price.inStock ? 'text-green-600' : 'text-red-600'}`}>
                        {price.inStock ? 'In Stock' : 'Out of Stock'}
                      </span>
                    </div>
                    <Button
                      className="w-full"
                      onClick={() => window.open(price.productUrl, '_blank')}
                      disabled={!price.inStock}
                    >
                      Buy at {price.store.name}
                      <ExternalLink size={16} className="ml-2" />
                    </Button>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>

        {/* Similar Products */}
        {similarProducts && similarProducts.length > 0 && (
          <div className="mt-12">
            <h2 className="text-2xl font-bold mb-6 text-slate-900">Similar Products</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {similarProducts.map((p) => (
                <Card
                  key={p.id}
                  className="overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
                  onClick={() => navigate(`/product/${p.id}`)}
                >
                  <div className="aspect-square overflow-hidden bg-slate-100">
                    <img
                      src={p.imageUrl || '/placeholder.jpg'}
                      alt={p.name}
                      className="w-full h-full object-cover hover:scale-105 transition-transform"
                    />
                  </div>
                  <CardContent className="p-4">
                    {p.brand && (
                      <Badge variant="secondary" className="mb-2">
                        {p.brand.name}
                      </Badge>
                    )}
                    <h3 className="font-semibold mb-2 text-slate-900 line-clamp-2">
                      {p.name}
                    </h3>
                    <div className="text-xl font-bold text-slate-900">
                      {p.prices[0]?.currency || '€'}{p.lowestPrice ?? Math.min(...p.prices.map((price) => price.price))}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
