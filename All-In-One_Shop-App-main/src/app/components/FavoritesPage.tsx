import { useNavigate } from 'react-router';
import { Heart, ArrowLeft, ShoppingBag, Loader2 } from 'lucide-react';
import { useFavorites } from '../api/hooks';
import { useAuth } from '../context/AuthContext';
import { ProductCard } from './ProductCard';
import { Button } from './ui/button';
import { UserMenu } from './UserMenu';

export function FavoritesPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { data: favorites, isLoading, error } = useFavorites();

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-slate-50">
        <header className="bg-white border-b sticky top-0 z-10">
          <div className="container mx-auto px-4 py-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-4">
                <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
                  <ArrowLeft className="h-5 w-5" />
                </Button>
                <h1 
                  className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent cursor-pointer"
                  onClick={() => navigate('/')}
                >
                  AllInOne Shop
                </h1>
              </div>
              <UserMenu />
            </div>
          </div>
        </header>
        <div className="container mx-auto px-4 py-16 text-center">
          <Heart className="h-16 w-16 mx-auto text-slate-300 mb-4" />
          <h2 className="text-2xl font-bold text-slate-900 mb-2">
            Sign in to view your favorites
          </h2>
          <p className="text-slate-600 mb-6">
            Create an account or sign in to save your favorite products
          </p>
          <Button onClick={() => navigate('/')}>Go to Home</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b sticky top-0 z-10">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
                <ArrowLeft className="h-5 w-5" />
              </Button>
              <h1 
                className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent cursor-pointer"
                onClick={() => navigate('/')}
              >
                AllInOne Shop
              </h1>
            </div>
            <UserMenu />
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">My Favorites</h2>
            {!isLoading && favorites && favorites.length > 0 && (
              <p className="text-slate-600">{favorites.length} saved items</p>
            )}
          </div>
          <Button variant="outline" onClick={() => navigate('/search')}>
            <ShoppingBag className="h-4 w-4 mr-2" />
            Continue Shopping
          </Button>
        </div>

        {isLoading ? (
          <div className="flex items-center justify-center py-16">
            <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
          </div>
        ) : error ? (
          <div className="text-center py-16">
            <p className="text-red-600 mb-4">Failed to load favorites. Please try again.</p>
            <Button onClick={() => window.location.reload()}>Retry</Button>
          </div>
        ) : favorites && favorites.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {favorites.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                showFavoriteButton
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-16">
            <Heart className="h-16 w-16 mx-auto text-slate-300 mb-4" />
            <h2 className="text-2xl font-bold text-slate-900 mb-2">
              No favorites yet
            </h2>
            <p className="text-slate-600 mb-6">
              Start adding products to your favorites by clicking the heart icon
            </p>
            <Button onClick={() => navigate('/search')}>Browse Products</Button>
          </div>
        )}
      </div>
    </div>
  );
}
