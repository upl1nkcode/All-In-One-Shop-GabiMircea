'use client';

import { useState, useEffect } from 'react';
import { Package, Store as StoreIcon, DollarSign, Search, Play, Loader2 } from 'lucide-react';
import { productApi, storeApi, catalogApi, adminApi } from '@/lib/api';
import { ProductsTab } from '@/components/admin/ProductsTab';
import { StoresTab } from '@/components/admin/StoresTab';
import { StatisticsTab } from '@/components/admin/StatisticsTab';
import { toast } from 'sonner';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState<'products'|'stores'|'statistics'>('products');
  const [stats, setStats] = useState({
    totalProducts: 0,
    totalStores: 0,
    totalPrices: 0,
  });
  
  // App-level data
  const [products, setProducts] = useState<any[]>([]);
  const [stores, setStores] = useState<any[]>([]);
  const [brands, setBrands] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [scraping, setScraping] = useState(false);

  const handleRunScraper = async () => {
    setScraping(true);
    try {
      await adminApi.runScraper();
      toast.success('Scraper triggered! Products will appear as they are scraped. Refresh in a minute.');
      // Reload after a delay to pick up newly scraped products
      setTimeout(() => loadData(), 15000);
      setTimeout(() => { loadData(); setScraping(false); }, 45000);
    } catch (err: any) {
      toast.error(err.message || 'Scraper failed');
      setScraping(false);
    }
  };

  const loadData = async () => {
    setLoading(true);
    try {
      const [prodRes, storeRes, brandRes, catRes] = await Promise.all([
        productApi.getAll({ size: 100 }), // Temp load all for dashboard
        storeApi.getAll(),
        catalogApi.getBrands(),
        catalogApi.getCategories(),
      ]);
      
      const prods = (prodRes.data as any)?.content || prodRes.data || [];
      const strs = storeRes.data || [];
      
      setProducts(prods);
      setStores(strs);
      setBrands(brandRes.data || []);
      setCategories(catRes.data || []);
      
      setStats({
        totalProducts: prods.length,
        totalStores: strs.length,
        totalPrices: prods.reduce((sum: number, p: any) => sum + (p.prices?.length || 0), 0),
      });
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const metricCards = [
    { label: 'Total Products', value: stats.totalProducts, icon: Package },
    { label: 'Active Stores', value: stats.totalStores, icon: StoreIcon },
    { label: 'Price Entries', value: stats.totalPrices, icon: DollarSign },
  ];

  return (
    <div className="w-full space-y-6">
      
      {/* Top Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {metricCards.map((card) => {
          const Icon = card.icon;
          return (
            <div key={card.label} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between items-start gap-4">
              <div className="flex items-center gap-2">
                <div className="bg-gray-100 text-gray-700 p-1.5 rounded-lg border border-gray-200">
                  <Icon className="w-4 h-4" />
                </div>
                <span className="text-sm font-semibold text-gray-600 uppercase tracking-wide">{card.label}</span>
              </div>
              <p className="text-4xl font-bold text-gray-900">{card.value}</p>
            </div>
          );
        })}
      </div>

      {/* Main Panel */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">

        {/* Header Block inside Panel */}
        <div className="flex flex-col md:flex-row md:items-center justify-between pb-6 border-b border-gray-100 gap-4">
          <div>
            <h2 className="text-base font-semibold text-gray-900">Manage Data</h2>
            <p className="text-sm text-gray-500">Add, edit, or remove products and stores</p>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={handleRunScraper}
              disabled={scraping}
              className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {scraping ? <Loader2 className="w-4 h-4 animate-spin" /> : <Play className="w-4 h-4" />}
              {scraping ? 'Scraping...' : 'Run Scraper'}
            </button>
            <div className="relative w-full md:w-64">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search..."
                className="w-full pl-9 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-shadow"
              />
            </div>
          </div>
        </div>

        {/* Custom Tabs */}
        <div className="flex items-center gap-2 mt-6 p-1 bg-gray-50 rounded-xl w-fit">
          <button 
            onClick={() => setActiveTab('products')}
            className={`px-8 py-2 text-sm font-medium rounded-lg transition-colors ${activeTab === 'products' ? 'bg-white text-gray-900 shadow-sm border border-gray-200/50' : 'text-gray-500 hover:text-gray-700'}`}
          >
            Products
          </button>
          <button 
            onClick={() => setActiveTab('stores')}
            className={`px-8 py-2 text-sm font-medium rounded-lg transition-colors ${activeTab === 'stores' ? 'bg-white text-gray-900 shadow-sm border border-gray-200/50' : 'text-gray-500 hover:text-gray-700'}`}
          >
            Stores
          </button>
          <button 
            onClick={() => setActiveTab('statistics')}
            className={`px-8 py-2 text-sm font-medium rounded-lg transition-colors ${activeTab === 'statistics' ? 'bg-white text-gray-900 shadow-sm border border-gray-200/50' : 'text-gray-500 hover:text-gray-700'}`}
          >
            Statistics
          </button>
        </div>

        {/* Tab Content */}
        <div className="mt-8">
          {loading ? (
            <div className="flex justify-center py-12">
              <span className="w-8 h-8 border-3 border-brand-600 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : (
            <>
              {activeTab === 'products' && (
                <ProductsTab products={products} stores={stores} categories={categories} brands={brands} onReload={loadData} />
              )}
              {activeTab === 'stores' && (
                <StoresTab stores={stores} onReload={loadData} />
              )}
              {activeTab === 'statistics' && (
                <StatisticsTab products={products} stores={stores} categories={categories} brands={brands} />
              )}
            </>
          )}
        </div>

      </div>
    </div>
  );
}
