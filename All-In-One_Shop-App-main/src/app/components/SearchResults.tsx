import { Search, SlidersHorizontal, X, Loader2 } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router';
import { useState, useEffect } from 'react';
import { useProductSearch, useBrands, useCategories, useStores } from '../api/hooks';
import { ProductCard } from './ProductCard';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Checkbox } from './ui/checkbox';
import { Label } from './ui/label';
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from './ui/sheet';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { UserMenu } from './UserMenu';
import type { SearchRequest } from '../api/types';

export function SearchResults() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const queryParam = searchParams.get('q') || '';
  const categoryParam = searchParams.get('category') || '';

  const [searchQuery, setSearchQuery] = useState(queryParam);
  const [selectedBrandIds, setSelectedBrandIds] = useState<string[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>('');
  const [selectedStoreIds, setSelectedStoreIds] = useState<string[]>([]);
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 500]);
  const [sortBy, setSortBy] = useState<SearchRequest['sortBy']>('price_asc');

  // Fetch catalog data
  const { data: brands = [] } = useBrands();
  const { data: categories = [] } = useCategories();
  const { data: stores = [] } = useStores();

  // Product search
  const { data: products = [], isLoading, mutate: searchProducts } = useProductSearch({
    query: queryParam,
    brandIds: selectedBrandIds.length > 0 ? selectedBrandIds : undefined,
    categoryIds: selectedCategoryId ? [selectedCategoryId] : undefined,
    storeIds: selectedStoreIds.length > 0 ? selectedStoreIds : undefined,
    minPrice: priceRange[0] > 0 ? priceRange[0] : undefined,
    maxPrice: priceRange[1] < 500 ? priceRange[1] : undefined,
    sortBy,
  });

  // Initial search and when params change
  useEffect(() => {
    const request: SearchRequest = {
      query: queryParam || undefined,
      brandIds: selectedBrandIds.length > 0 ? selectedBrandIds : undefined,
      categoryIds: selectedCategoryId ? [selectedCategoryId] : undefined,
      storeIds: selectedStoreIds.length > 0 ? selectedStoreIds : undefined,
      minPrice: priceRange[0] > 0 ? priceRange[0] : undefined,
      maxPrice: priceRange[1] < 500 ? priceRange[1] : undefined,
      sortBy,
    };
    searchProducts(request);
  }, [queryParam, selectedBrandIds, selectedCategoryId, selectedStoreIds, priceRange, sortBy, searchProducts]);

  // Set category from URL param
  useEffect(() => {
    if (categoryParam && categories.length > 0) {
      const category = categories.find(c => 
        c.name.toLowerCase() === categoryParam.toLowerCase() || 
        c.slug === categoryParam.toLowerCase()
      );
      if (category) {
        setSelectedCategoryId(category.id);
      }
    }
  }, [categoryParam, categories]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery)}`);
    }
  };

  const toggleBrand = (brandId: string) => {
    setSelectedBrandIds((prev) =>
      prev.includes(brandId) ? prev.filter((b) => b !== brandId) : [...prev, brandId]
    );
  };

  const toggleStore = (storeId: string) => {
    setSelectedStoreIds((prev) =>
      prev.includes(storeId) ? prev.filter((s) => s !== storeId) : [...prev, storeId]
    );
  };

  const clearFilters = () => {
    setSelectedBrandIds([]);
    setSelectedCategoryId('');
    setSelectedStoreIds([]);
    setPriceRange([0, 500]);
  };

  const activeFiltersCount =
    selectedBrandIds.length +
    selectedStoreIds.length +
    (selectedCategoryId ? 1 : 0) +
    (priceRange[0] > 0 || priceRange[1] < 500 ? 1 : 0);

  const FiltersContent = () => (
    <div className="space-y-6">
      {/* Category Filter */}
      <div>
        <h4 className="font-semibold mb-3 text-slate-900">Category</h4>
        <div className="space-y-2">
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="radio"
              name="category"
              checked={!selectedCategoryId}
              onChange={() => setSelectedCategoryId('')}
              className="w-4 h-4 text-blue-600"
            />
            <span className="text-slate-700">All</span>
          </label>
          {categories.map((category) => (
            <label key={category.id} className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="category"
                checked={selectedCategoryId === category.id}
                onChange={() => setSelectedCategoryId(category.id)}
                className="w-4 h-4 text-blue-600"
              />
              <span className="text-slate-700">{category.name}</span>
            </label>
          ))}
        </div>
      </div>

      {/* Brand Filter */}
      <div>
        <h4 className="font-semibold mb-3 text-slate-900">Brand</h4>
        <div className="space-y-2">
          {brands.map((brand) => (
            <div key={brand.id} className="flex items-center gap-2">
              <Checkbox
                id={`brand-${brand.id}`}
                checked={selectedBrandIds.includes(brand.id)}
                onCheckedChange={() => toggleBrand(brand.id)}
              />
              <Label htmlFor={`brand-${brand.id}`} className="text-slate-700 cursor-pointer">
                {brand.name}
              </Label>
            </div>
          ))}
        </div>
      </div>

      {/* Store Filter */}
      <div>
        <h4 className="font-semibold mb-3 text-slate-900">Store</h4>
        <div className="space-y-2">
          {stores.map((store) => (
            <div key={store.id} className="flex items-center gap-2">
              <Checkbox
                id={`store-${store.id}`}
                checked={selectedStoreIds.includes(store.id)}
                onCheckedChange={() => toggleStore(store.id)}
              />
              <Label htmlFor={`store-${store.id}`} className="text-slate-700 cursor-pointer">
                {store.name}
              </Label>
            </div>
          ))}
        </div>
      </div>

      {/* Price Range */}
      <div>
        <h4 className="font-semibold mb-3 text-slate-900">Price Range</h4>
        <div className="space-y-2">
          <div className="flex gap-2">
            <Input
              type="number"
              value={priceRange[0]}
              onChange={(e) => setPriceRange([parseInt(e.target.value) || 0, priceRange[1]])}
              className="w-24"
              placeholder="Min"
            />
            <span className="self-center text-slate-600">-</span>
            <Input
              type="number"
              value={priceRange[1]}
              onChange={(e) => setPriceRange([priceRange[0], parseInt(e.target.value) || 500])}
              className="w-24"
              placeholder="Max"
            />
          </div>
        </div>
      </div>

      <Button onClick={clearFilters} variant="outline" className="w-full">
        Clear All Filters
      </Button>
    </div>
  );

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <header className="bg-white border-b sticky top-0 z-10">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center gap-4">
            <h1
              className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent cursor-pointer"
              onClick={() => navigate('/')}
            >
              AllInOne Shop
            </h1>
            <form onSubmit={handleSearch} className="flex-1 max-w-2xl">
              <div className="relative flex items-center">
                <Search className="absolute left-3 text-slate-400" size={20} />
                <Input
                  type="text"
                  placeholder="Search products..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-10 pr-4"
                />
              </div>
            </form>
            <UserMenu />
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-6">
        <div className="flex flex-col md:flex-row gap-6">
          {/* Desktop Filters Sidebar */}
          <aside className="hidden md:block w-64 flex-shrink-0">
            <div className="bg-white rounded-lg p-6 sticky top-24">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-bold text-lg text-slate-900">Filters</h3>
                {activeFiltersCount > 0 && (
                  <span className="text-sm text-blue-600">
                    {activeFiltersCount} active
                  </span>
                )}
              </div>
              <FiltersContent />
            </div>
          </aside>

          {/* Main Content */}
          <div className="flex-1">
            {/* Results Header */}
            <div className="bg-white rounded-lg p-4 mb-6">
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div>
                  <h2 className="font-bold text-xl text-slate-900">
                    {queryParam ? `Results for "${queryParam}"` : 'All Products'}
                  </h2>
                  <p className="text-slate-600">
                    {isLoading ? 'Searching...' : `${products.length} products found`}
                  </p>
                </div>
                <div className="flex items-center gap-2 w-full sm:w-auto">
                  {/* Mobile Filter Button */}
                  <Sheet>
                    <SheetTrigger asChild>
                      <Button variant="outline" className="md:hidden flex-1 sm:flex-none">
                        <SlidersHorizontal size={16} className="mr-2" />
                        Filters
                        {activeFiltersCount > 0 && (
                          <span className="ml-2 bg-blue-600 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs">
                            {activeFiltersCount}
                          </span>
                        )}
                      </Button>
                    </SheetTrigger>
                    <SheetContent side="left">
                      <SheetHeader>
                        <SheetTitle>Filters</SheetTitle>
                      </SheetHeader>
                      <div className="mt-6">
                        <FiltersContent />
                      </div>
                    </SheetContent>
                  </Sheet>

                  <Select 
                    value={sortBy} 
                    onValueChange={(value) => setSortBy(value as SearchRequest['sortBy'])}
                  >
                    <SelectTrigger className="w-[180px]">
                      <SelectValue placeholder="Sort by" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="price_asc">Price: Low to High</SelectItem>
                      <SelectItem value="price_desc">Price: High to Low</SelectItem>
                      <SelectItem value="name_asc">Name A-Z</SelectItem>
                      <SelectItem value="name_desc">Name Z-A</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            {/* Active Filters Pills */}
            {activeFiltersCount > 0 && (
              <div className="flex flex-wrap gap-2 mb-6">
                {selectedBrandIds.map((brandId) => {
                  const brand = brands.find((b) => b.id === brandId);
                  return (
                    <button
                      key={brandId}
                      onClick={() => toggleBrand(brandId)}
                      className="bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm flex items-center gap-1 hover:bg-blue-200"
                    >
                      {brand?.name}
                      <X size={14} />
                    </button>
                  );
                })}
                {selectedStoreIds.map((storeId) => {
                  const store = stores.find((s) => s.id === storeId);
                  return (
                    <button
                      key={storeId}
                      onClick={() => toggleStore(storeId)}
                      className="bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm flex items-center gap-1 hover:bg-blue-200"
                    >
                      {store?.name}
                      <X size={14} />
                    </button>
                  );
                })}
                {selectedCategoryId && (
                  <button
                    onClick={() => setSelectedCategoryId('')}
                    className="bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm flex items-center gap-1 hover:bg-blue-200"
                  >
                    {categories.find(c => c.id === selectedCategoryId)?.name}
                    <X size={14} />
                  </button>
                )}
              </div>
            )}

            {/* Product Grid */}
            {isLoading ? (
              <div className="flex items-center justify-center py-12">
                <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
              </div>
            ) : products.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
            ) : (
              <div className="bg-white rounded-lg p-12 text-center">
                <p className="text-lg text-slate-600">No products found matching your criteria</p>
                <Button onClick={clearFilters} className="mt-4">
                  Clear Filters
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
