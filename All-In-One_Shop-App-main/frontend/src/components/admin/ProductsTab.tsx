import { useState } from 'react';
import { Edit2, Trash2, Plus } from 'lucide-react';
import { ProductModal } from './ProductModal';
import { productApi } from '@/lib/api';
import { toast } from 'sonner';

export function ProductsTab({ products, stores, categories, brands, onReload }: any) {
  const [modalOpen, setModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<any>(null);

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
      await productApi.delete(id);
      toast.success('Product deleted successfully');
      onReload();
    } catch (err: any) {
      toast.error(err.message || 'Failed to delete product');
    }
  };

  const openEdit = (product: any) => {
    setEditingProduct(product);
    setModalOpen(true);
  };

  const openAdd = () => {
    setEditingProduct(null);
    setModalOpen(true);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-semibold text-gray-900">Products ({products.length})</h3>
        <button onClick={openAdd} className="btn-primary flex items-center gap-2 text-sm py-2">
          <Plus className="w-4 h-4" /> Add Product
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm text-left text-gray-500">
          <thead className="text-xs text-gray-700 uppercase bg-gray-50 border-b border-gray-100">
            <tr>
              <th className="px-6 py-3 font-semibold">Image</th>
              <th className="px-6 py-3 font-semibold">Name</th>
              <th className="px-6 py-3 font-semibold">Brand</th>
              <th className="px-6 py-3 font-semibold">Category</th>
              <th className="px-6 py-3 font-semibold">Price Range</th>
              <th className="px-6 py-3 font-semibold">Stores</th>
              <th className="px-6 py-3 font-semibold text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product: any) => {
              const brand = product.brand?.name || brands.find((b: any) => b.id === product.brandId)?.name || '-';
              const category = product.category?.name || categories.find((c: any) => c.id === product.categoryId)?.name || '-';

              const minPrice = product.lowestPrice ?? (product.prices?.length ? Math.min(...product.prices.map((p: any) => p.price)) : null);
              const maxPrice = product.highestPrice ?? (product.prices?.length ? Math.max(...product.prices.map((p: any) => p.price)) : null);
              const priceRange = minPrice != null ? `€${minPrice} - €${maxPrice}` : 'N/A';
              
              return (
                <tr key={product.id} className="bg-white border-b border-gray-50 hover:bg-gray-50/50 transition-colors">
                  <td className="px-6 py-4">
                    <img src={product.imageUrl || '/placeholder.png'} alt={product.name} className="w-10 h-10 rounded-lg object-cover bg-gray-100" />
                  </td>
                  <td className="px-6 py-4 font-medium text-gray-900">{product.name}</td>
                  <td className="px-6 py-4">{brand}</td>
                  <td className="px-6 py-4">{category}</td>
                  <td className="px-6 py-4">{priceRange}</td>
                  <td className="px-6 py-4">{product.prices?.length || 0}</td>
                  <td className="px-6 py-4 text-right space-x-2">
                    <button onClick={() => openEdit(product)} className="text-gray-400 hover:text-brand-600 transition-colors" title="Edit">
                      <Edit2 className="w-4 h-4 inline" />
                    </button>
                    <button onClick={() => handleDelete(product.id)} className="text-gray-400 hover:text-red-600 transition-colors" title="Delete">
                      <Trash2 className="w-4 h-4 inline" />
                    </button>
                  </td>
                </tr>
              );
            })}
            {products.length === 0 && (
              <tr>
                <td colSpan={7} className="px-6 py-8 text-center text-gray-500">No products found. Add one or run the scraper.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {modalOpen && (
        <ProductModal 
          product={editingProduct}
          stores={stores}
          categories={categories}
          brands={brands}
          onClose={() => setModalOpen(false)} 
          onSuccess={() => {
            setModalOpen(false);
            onReload();
          }} 
        />
      )}
    </div>
  );
}
