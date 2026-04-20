import { useState } from 'react';
import { storeApi } from '@/lib/api';
import { toast } from 'sonner';

export function StoreModal({ store, onClose, onSuccess }: any) {
  const [formData, setFormData] = useState({
    name: store?.name || '',
    website: store?.website || '',
    logoUrl: store?.logoUrl || '',
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: any) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (store?.id) {
        await storeApi.update(store.id, formData);
        toast.success('Store updated');
      } else {
        await storeApi.create(formData);
        toast.success('Store created');
      }
      onSuccess();
    } catch (err: any) {
      toast.error(err.message || 'Operation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-gray-900/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="p-6 border-b border-gray-100 flex justify-between items-center">
          <h2 className="text-xl font-bold text-gray-900">{store ? 'Edit Store' : 'Add New Store'}</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">✕</button>
        </div>
        
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Store Name *</label>
            <input type="text" required value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="input-field" placeholder="Store Name" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Website URL *</label>
            <input type="url" required value={formData.website} onChange={e => setFormData({...formData, website: e.target.value})} className="input-field" placeholder="https://example.com" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Logo URL</label>
            <input type="url" value={formData.logoUrl} onChange={e => setFormData({...formData, logoUrl: e.target.value})} className="input-field" placeholder="https://domain.com/logo.png" />
          </div>

          <div className="pt-4 flex justify-end gap-3">
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Saving...' : (store ? 'Update' : 'Add')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
