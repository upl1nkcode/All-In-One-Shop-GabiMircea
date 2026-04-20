import { useState } from 'react';
import { productApi } from '@/lib/api';
import { toast } from 'sonner';

interface FormErrors {
  name?: string;
  brandId?: string;
  categoryId?: string;
  description?: string;
  imageUrl?: string;
}

function validate(formData: { name: string; brandId: string; categoryId: string; description: string; imageUrl: string; gender: string }): FormErrors {
  const errors: FormErrors = {};

  if (!formData.name.trim()) {
    errors.name = 'Product name is required.';
  } else if (formData.name.trim().length < 3) {
    errors.name = 'Name must be at least 3 characters.';
  } else if (formData.name.trim().length > 500) {
    errors.name = 'Name must be 500 characters or fewer.';
  }

  if (!formData.brandId) {
    errors.brandId = 'Please select a brand.';
  }

  if (!formData.categoryId) {
    errors.categoryId = 'Please select a category.';
  }

  if (formData.description && formData.description.length > 2000) {
    errors.description = 'Description must be 2000 characters or fewer.';
  }

  if (formData.imageUrl && formData.imageUrl.trim()) {
    try {
      new URL(formData.imageUrl.trim());
    } catch {
      errors.imageUrl = 'Image URL must be a valid URL (e.g. https://…).';
    }
  }

  return errors;
}

export function ProductModal({ product, stores, categories, brands, onClose, onSuccess }: any) {
  const [formData, setFormData] = useState({
    name: product?.name || '',
    brandId: product?.brandId || '',
    categoryId: product?.categoryId || '',
    description: product?.description || '',
    imageUrl: product?.imageUrl || '',
    gender: product?.gender || 'UNISEX',
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: any) => {
    e.preventDefault();
    const validationErrors = validate(formData);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    setErrors({});
    setLoading(true);
    try {
      if (product?.id) {
        await productApi.update(product.id, formData);
        toast.success('Product updated');
      } else {
        await productApi.create(formData);
        toast.success('Product created');
      }
      onSuccess();
    } catch (err: any) {
      toast.error(err.message || 'Operation failed');
    } finally {
      setLoading(false);
    }
  };

  const field = (key: keyof FormErrors) => ({
    onChange: (e: any) => {
      setFormData(f => ({ ...f, [key]: e.target.value }));
      if (errors[key]) setErrors(prev => ({ ...prev, [key]: undefined }));
    },
  });

  return (
    <div className="fixed inset-0 bg-gray-900/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="p-6 border-b border-gray-100 flex justify-between items-center sticky top-0 bg-white">
          <h2 className="text-xl font-bold">{product ? 'Edit Product' : 'Add New Product'}</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">✕</button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-5" noValidate>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Product Name *</label>
              <input
                type="text"
                value={formData.name}
                {...field('name')}
                className={`input-field ${errors.name ? 'border-red-500' : ''}`}
                placeholder="Classic Black Hoodie"
              />
              {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Brand *</label>
              <select
                value={formData.brandId}
                {...field('brandId')}
                className={`input-field ${errors.brandId ? 'border-red-500' : ''}`}
              >
                <option value="">Select Brand</option>
                {brands.map((b: any) => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
              {errors.brandId && <p className="text-red-500 text-xs mt-1">{errors.brandId}</p>}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Category *</label>
            <select
              value={formData.categoryId}
              {...field('categoryId')}
              className={`input-field ${errors.categoryId ? 'border-red-500' : ''}`}
            >
              <option value="">Select Category</option>
              {categories.map((c: any) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            {errors.categoryId && <p className="text-red-500 text-xs mt-1">{errors.categoryId}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
              {formData.description.length > 0 && (
                <span className={`ml-2 text-xs font-normal ${formData.description.length > 2000 ? 'text-red-500' : 'text-gray-400'}`}>
                  {formData.description.length}/2000
                </span>
              )}
            </label>
            <textarea
              value={formData.description}
              {...field('description')}
              className={`input-field ${errors.description ? 'border-red-500' : ''}`}
              rows={3}
              placeholder="Detailed product description..."
            />
            {errors.description && <p className="text-red-500 text-xs mt-1">{errors.description}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
            <input
              type="text"
              value={formData.imageUrl}
              {...field('imageUrl')}
              className={`input-field ${errors.imageUrl ? 'border-red-500' : ''}`}
              placeholder="https://images.unsplash.com/..."
            />
            {errors.imageUrl && <p className="text-red-500 text-xs mt-1">{errors.imageUrl}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Gender</label>
            <select
              value={formData.gender}
              onChange={e => setFormData(f => ({ ...f, gender: e.target.value }))}
              className="input-field"
            >
              <option value="UNISEX">UNISEX</option>
              <option value="MEN">MEN</option>
              <option value="WOMEN">WOMEN</option>
              <option value="KIDS">KIDS</option>
            </select>
          </div>

          <div className="pt-4 flex justify-end gap-3">
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={loading} className="btn-primary" style={{ backgroundColor: '#0f172a' }}>
              {loading ? 'Saving...' : (product ? 'Update Product' : 'Add Product')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
