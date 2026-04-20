import { useState } from 'react';
import { Edit2, Trash2, Plus, ExternalLink } from 'lucide-react';
import { storeApi } from '@/lib/api';
import { StoreModal } from './StoreModal';
import { toast } from 'sonner';

export function StoresTab({ stores, onReload }: any) {
  const [modalOpen, setModalOpen] = useState(false);
  const [editingStore, setEditingStore] = useState<any>(null);

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this store?')) return;
    try {
      await storeApi.delete(id);
      toast.success('Store deleted successfully');
      onReload();
    } catch (err: any) {
      toast.error(err.message || 'Failed to delete store');
    }
  };

  const openEdit = (store: any) => {
    setEditingStore(store);
    setModalOpen(true);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-semibold text-gray-900">Stores ({stores.length})</h3>
        <button onClick={() => { setEditingStore(null); setModalOpen(true); }} className="btn-primary flex items-center gap-2 text-sm py-2" style={{ backgroundColor: '#0f172a' }}>
          <Plus className="w-4 h-4" /> Add Store
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {stores.map((store: any) => (
          <div key={store.id} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h4 className="font-bold text-gray-900 text-lg">{store.name}</h4>
                <a href={store.website} target="_blank" rel="noopener noreferrer" className="text-sm text-brand-600 hover:underline flex items-center gap-1 mt-1">
                  {new URL(store.website).hostname.replace('www.', '')} <ExternalLink className="w-3 h-3" />
                </a>
              </div>
              <div className="flex gap-2">
                <button onClick={() => openEdit(store)} className="text-gray-400 hover:text-brand-600 transition-colors p-1" title="Edit">
                  <Edit2 className="w-4 h-4" />
                </button>
                <button onClick={() => handleDelete(store.id)} className="text-gray-400 hover:text-red-600 transition-colors p-1" title="Delete">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
            
            <div className="mt-4 pt-4 border-t border-gray-50 flex justify-between items-center">
              <span className="text-sm text-gray-500">Active Listing</span>
              <span className="bg-green-100 text-green-700 text-xs font-bold px-2 py-1 rounded-full flex items-center gap-1">
                <span className="w-1.5 h-1.5 bg-green-500 rounded-full inline-block"></span> Active
              </span>
            </div>
          </div>
        ))}
      </div>

      {modalOpen && (
        <StoreModal
          store={editingStore}
          onClose={() => setModalOpen(false)}
          onSuccess={() => { setModalOpen(false); onReload(); }}
        />
      )}
    </div>
  );
}
