import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router';
import {
  ShieldAlert, Package, Store, Tag, DollarSign, TrendingUp,
  Play, Square, RefreshCw, ArrowLeft, Plus, Pencil, Trash2,
  Search, X, ExternalLink,
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell, LabelList,
} from 'recharts';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Skeleton } from './ui/skeleton';
import { Badge } from './ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import {
  Dialog, DialogContent, DialogDescription, DialogFooter,
  DialogHeader, DialogTitle,
} from './ui/dialog';
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from './ui/table';
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from './ui/select';
import { Textarea } from './ui/textarea';
import { useAuth } from '../context/AuthContext';
import {
  useAdminStats, useFakerStatus, useRunScraper, useStartFaker, useStopFaker,
  useAdminProducts, useIngestProduct, useUpdateProduct, useDeleteProduct,
  useStores, useCategories, useBrands,
  useCreateStore, useUpdateStore, useDeleteStore,
} from '../api/hooks';
import type { Product, Store as StoreType } from '../api/types';
import { toast } from 'sonner';

// ─── colour palette for charts ─────────────────────────────────────────────
const CHART_COLORS = ['#6366f1', '#ec4899', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6', '#ef4444', '#14b8a6'];

// ─── Stat Card ──────────────────────────────────────────────────────────────
function StatCard({ icon: Icon, label, value, color }: {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  value: string | number | undefined;
  color: string;
}) {
  return (
    <Card>
      <CardContent className="flex items-center gap-4 pt-6">
        <div className={`p-3 rounded-full ${color}`}>
          <Icon className="h-6 w-6 text-white" />
        </div>
        <div>
          <p className="text-sm text-muted-foreground">{label}</p>
          {value !== undefined ? (
            <p className="text-2xl font-bold">{value}</p>
          ) : (
            <Skeleton className="h-8 w-20 mt-1" />
          )}
        </div>
      </CardContent>
    </Card>
  );
}

// ─── Validation helpers ─────────────────────────────────────────────────────
function isValidUrl(v: string) {
  try { new URL(v); return true; } catch { return false; }
}

type ProductFormErrors = {
  name?: string; brand?: string; category?: string;
  description?: string; imageUrl?: string;
  prices?: string;
};

function validateProductForm(
  form: { name: string; brand: string; category: string; description: string; imageUrl: string },
  prices: { storeId: string; storeName: string; price: string; productUrl: string }[]
): ProductFormErrors {
  const e: ProductFormErrors = {};
  if (!form.name.trim()) e.name = 'Product name is required';
  else if (form.name.trim().length < 2) e.name = 'Minimum 2 characters';
  if (!form.brand.trim()) e.brand = 'Brand is required';
  if (!form.category.trim()) e.category = 'Category is required';
  if (!form.description.trim()) e.description = 'Description is required';
  if (!form.imageUrl.trim()) e.imageUrl = 'Image URL is required';
  else if (!isValidUrl(form.imageUrl)) e.imageUrl = 'Must be a valid URL';
  if (prices.length === 0) {
    e.prices = 'Add at least one store price';
  } else {
    for (const p of prices) {
      if (!p.storeName.trim()) { e.prices = 'Store name is required'; break; }
      const n = parseFloat(p.price);
      if (isNaN(n) || n <= 0) { e.prices = 'Price must be greater than 0'; break; }
      if (!p.productUrl.trim()) { e.prices = 'Product URL is required'; break; }
      if (!isValidUrl(p.productUrl)) { e.prices = 'Product URL must be a valid URL'; break; }
    }
  }
  return e;
}

type StoreFormErrors = { name?: string; website?: string };
function validateStoreForm(form: { name: string; website: string }): StoreFormErrors {
  const e: StoreFormErrors = {};
  if (!form.name.trim()) e.name = 'Store name is required';
  else if (form.name.trim().length < 2) e.name = 'Minimum 2 characters';
  if (!form.website.trim()) e.website = 'Website is required';
  else if (!isValidUrl(form.website)) e.website = 'Must be a valid URL (include https://)';
  return e;
}

// ─── Add / Edit Product Dialog ───────────────────────────────────────────────
type PriceEntry = { storeId: string; storeName: string; price: string; productUrl: string };

const BLANK_PRICE: PriceEntry = { storeId: '', storeName: '', price: '', productUrl: '' };

function ProductDialog({
  open, onClose, product, stores, categories, brands, onSaved,
}: {
  open: boolean;
  onClose: () => void;
  product: Product | null;
  stores: StoreType[];
  categories: { id: string; name: string }[];
  brands: { id: string; name: string }[];
  onSaved: () => void;
}) {
  const isEdit = !!product;
  const { trigger: ingest, isMutating: ingesting } = useIngestProduct();
  const { trigger: update, isMutating: updating } = useUpdateProduct();

  const [form, setForm] = useState({
    name: product?.name ?? '',
    brand: product?.brand?.name ?? '',
    category: product?.category?.name ?? '',
    description: product?.description ?? '',
    imageUrl: product?.imageUrl ?? '',
  });
  const [prices, setPrices] = useState<PriceEntry[]>(
    product?.prices?.length
      ? product.prices.map(p => ({
          storeId: p.store.id,
          storeName: p.store.name,
          price: String(p.price),
          productUrl: p.productUrl,
        }))
      : [{ ...BLANK_PRICE }]
  );
  const [errors, setErrors] = useState<ProductFormErrors>({});
  const isBusy = ingesting || updating;

  function setField(k: keyof typeof form, v: string) {
    setForm(f => ({ ...f, [k]: v }));
    setErrors(e => ({ ...e, [k]: undefined }));
  }
  function setPriceField(i: number, k: keyof PriceEntry, v: string) {
    setPrices(ps => ps.map((p, idx) => idx === i ? { ...p, [k]: v } : p));
    setErrors(e => ({ ...e, prices: undefined }));
  }
  function addPriceRow() { setPrices(ps => [...ps, { ...BLANK_PRICE }]); }
  function removePriceRow(i: number) { setPrices(ps => ps.filter((_, idx) => idx !== i)); }

  async function handleSubmit() {
    const errs = validateProductForm(form, prices);
    if (Object.keys(errs).length) { setErrors(errs); return; }

    try {
      if (isEdit) {
        await update({
          id: product!.id,
          dto: {
            name: form.name.trim(),
            description: form.description.trim(),
            imageUrl: form.imageUrl.trim(),
            brand: product!.brand ? { id: product!.brand.id, name: form.brand.trim() } : undefined,
            category: product!.category ? { id: product!.category.id, name: form.category.trim(), slug: form.category.toLowerCase().replace(/\s+/g, '-') } : undefined,
          },
        });
        toast.success('Product updated');
      } else {
        // Call ingest once per store price entry
        for (const p of prices) {
          await ingest({
            name: form.name.trim(),
            brand: form.brand.trim(),
            category: form.category.trim(),
            description: form.description.trim(),
            imageUrl: form.imageUrl.trim(),
            price: {
              storeName: p.storeName.trim(),
              price: parseFloat(p.price),
              currency: 'EUR',
              productUrl: p.productUrl.trim(),
              inStock: true,
            },
          });
        }
        toast.success('Product added');
      }
      onSaved();
      onClose();
    } catch {
      toast.error('Failed to save product');
    }
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Edit Product' : 'Add New Product'}</DialogTitle>
          <DialogDescription>
            {isEdit ? 'Update product details' : 'Fill in the details to add a new product'}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2">
          {/* Row: Name + Brand */}
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label htmlFor="p-name">Product Name <span className="text-red-500">*</span></Label>
              <Input id="p-name" placeholder="Classic Black Hoodie" value={form.name} onChange={e => setField('name', e.target.value)} />
              {errors.name && <p className="text-xs text-red-500">{errors.name}</p>}
            </div>
            <div className="space-y-1">
              <Label htmlFor="p-brand">Brand <span className="text-red-500">*</span></Label>
              <Input id="p-brand" placeholder="Nike" value={form.brand} onChange={e => setField('brand', e.target.value)}
                list="brands-list" />
              <datalist id="brands-list">
                {brands.map(b => <option key={b.id} value={b.name} />)}
              </datalist>
              {errors.brand && <p className="text-xs text-red-500">{errors.brand}</p>}
            </div>
          </div>

          {/* Category */}
          <div className="space-y-1">
            <Label htmlFor="p-cat">Category <span className="text-red-500">*</span></Label>
            <Input id="p-cat" placeholder="Hoodies" value={form.category} onChange={e => setField('category', e.target.value)}
              list="cats-list" />
            <datalist id="cats-list">
              {categories.map(c => <option key={c.id} value={c.name} />)}
            </datalist>
            {errors.category && <p className="text-xs text-red-500">{errors.category}</p>}
          </div>

          {/* Description */}
          <div className="space-y-1">
            <Label htmlFor="p-desc">Description <span className="text-red-500">*</span></Label>
            <Textarea id="p-desc" placeholder="Detailed product description..." rows={3}
              value={form.description} onChange={e => setField('description', e.target.value)} />
            {errors.description && <p className="text-xs text-red-500">{errors.description}</p>}
          </div>

          {/* Image URL */}
          <div className="space-y-1">
            <Label htmlFor="p-img">Image URL <span className="text-red-500">*</span></Label>
            <Input id="p-img" placeholder="https://images.unsplash.com/..." value={form.imageUrl}
              onChange={e => setField('imageUrl', e.target.value)} />
            {errors.imageUrl && <p className="text-xs text-red-500">{errors.imageUrl}</p>}
          </div>

          {/* Store Prices */}
          {!isEdit && (
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label>Store Prices <span className="text-red-500">*</span></Label>
                <Button type="button" variant="outline" size="sm" onClick={addPriceRow}>
                  <Plus className="h-3 w-3 mr-1" /> Add Store
                </Button>
              </div>
              {errors.prices && <p className="text-xs text-red-500">{errors.prices}</p>}
              {prices.map((p, i) => (
                <div key={i} className="border rounded-md p-3 space-y-2 relative">
                  {prices.length > 1 && (
                    <button type="button" onClick={() => removePriceRow(i)}
                      className="absolute top-2 right-2 text-muted-foreground hover:text-red-500">
                      <X className="h-3 w-3" />
                    </button>
                  )}
                  <div className="grid grid-cols-2 gap-2">
                    <div className="space-y-1">
                      <Label className="text-xs">Store</Label>
                      <Input placeholder="Store name" value={p.storeName}
                        onChange={e => {
                          setPriceField(i, 'storeName', e.target.value);
                          const found = stores.find(s => s.name === e.target.value);
                          if (found) setPriceField(i, 'storeId', found.id);
                        }}
                        list="stores-list" />
                      <datalist id="stores-list">
                        {stores.map(s => <option key={s.id} value={s.name} />)}
                      </datalist>
                    </div>
                    <div className="space-y-1">
                      <Label className="text-xs">Price (€)</Label>
                      <Input type="number" placeholder="0.00" min="0" step="0.01"
                        value={p.price} onChange={e => setPriceField(i, 'price', e.target.value)} />
                    </div>
                  </div>
                  <div className="space-y-1">
                    <Label className="text-xs">Product URL</Label>
                    <Input placeholder="https://store.com/product" value={p.productUrl}
                      onChange={e => setPriceField(i, 'productUrl', e.target.value)} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={isBusy}>Cancel</Button>
          <Button onClick={handleSubmit} disabled={isBusy}>
            {isBusy ? 'Saving…' : isEdit ? 'Save Changes' : 'Add Product'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ─── Add / Edit Store Dialog ─────────────────────────────────────────────────
function StoreDialog({
  open, onClose, store, onSaved,
}: {
  open: boolean;
  onClose: () => void;
  store: StoreType | null;
  onSaved: () => void;
}) {
  const isEdit = !!store;
  const { trigger: create, isMutating: creating } = useCreateStore();
  const { trigger: update, isMutating: updating } = useUpdateStore();

  const [form, setForm] = useState({ name: store?.name ?? '', website: store?.website ?? '' });
  const [errors, setErrors] = useState<StoreFormErrors>({});
  const isBusy = creating || updating;

  function setField(k: keyof typeof form, v: string) {
    setForm(f => ({ ...f, [k]: v }));
    setErrors(e => ({ ...e, [k]: undefined }));
  }

  async function handleSubmit() {
    const errs = validateStoreForm(form);
    if (Object.keys(errs).length) { setErrors(errs); return; }
    try {
      if (isEdit) {
        await update({ id: store!.id, dto: { name: form.name.trim(), website: form.website.trim() } });
        toast.success('Store updated');
      } else {
        await create({ name: form.name.trim(), website: form.website.trim(), isActive: true });
        toast.success('Store added');
      }
      onSaved();
      onClose();
    } catch {
      toast.error('Failed to save store');
    }
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Edit Store' : 'Add New Store'}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4 py-2">
          <div className="space-y-1">
            <Label>Store Name <span className="text-red-500">*</span></Label>
            <Input placeholder="ASOS" value={form.name} onChange={e => setField('name', e.target.value)} />
            {errors.name && <p className="text-xs text-red-500">{errors.name}</p>}
          </div>
          <div className="space-y-1">
            <Label>Website <span className="text-red-500">*</span></Label>
            <Input placeholder="https://asos.com" value={form.website} onChange={e => setField('website', e.target.value)} />
            {errors.website && <p className="text-xs text-red-500">{errors.website}</p>}
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={isBusy}>Cancel</Button>
          <Button onClick={handleSubmit} disabled={isBusy}>
            {isBusy ? 'Saving…' : isEdit ? 'Save Changes' : 'Add Store'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ─── Delete Confirm Dialog ───────────────────────────────────────────────────
function DeleteDialog({
  open, onClose, label, onConfirm, isBusy,
}: {
  open: boolean; onClose: () => void; label: string; onConfirm: () => void; isBusy: boolean;
}) {
  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Confirm Delete</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete <strong>{label}</strong>? This cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={isBusy}>Cancel</Button>
          <Button variant="destructive" onClick={onConfirm} disabled={isBusy}>
            {isBusy ? 'Deleting…' : 'Delete'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ─── Analytics Section ───────────────────────────────────────────────────────
function AnalyticsDashboard({ products }: { products: Product[] }) {
  const brandStats = useMemo(() => {
    const map = new Map<string, { name: string; products: number; totalPrice: number; minP: number; maxP: number; storeTotal: number }>();
    products.forEach(p => {
      if (!p.brand) return;
      const key = p.brand.name;
      if (!map.has(key)) map.set(key, { name: key, products: 0, totalPrice: 0, minP: Infinity, maxP: 0, storeTotal: 0 });
      const b = map.get(key)!;
      b.products++;
      b.storeTotal += p.storeCount || 0;
      const lp = p.lowestPrice ?? 0;
      if (lp > 0) { b.totalPrice += lp; b.minP = Math.min(b.minP, lp); b.maxP = Math.max(b.maxP, lp); }
    });
    return Array.from(map.values())
      .map(b => ({
        name: b.name,
        products: b.products,
        avgPrice: b.products > 0 ? Math.round(b.totalPrice / b.products) : 0,
        minPrice: b.minP === Infinity ? 0 : Math.round(b.minP),
        maxPrice: Math.round(b.maxP),
        avgStores: b.products > 0 ? Math.round(b.storeTotal / b.products) : 0,
      }))
      .sort((a, b) => b.products - a.products)
      .slice(0, 8);
  }, [products]);

  const categoryStats = useMemo(() => {
    const map = new Map<string, { name: string; count: number; totalPrice: number; brands: Set<string> }>();
    products.forEach(p => {
      if (!p.category) return;
      const key = p.category.name;
      if (!map.has(key)) map.set(key, { name: key, count: 0, totalPrice: 0, brands: new Set() });
      const c = map.get(key)!;
      c.count++;
      if (p.lowestPrice) c.totalPrice += p.lowestPrice;
      if (p.brand) c.brands.add(p.brand.name);
    });
    return Array.from(map.values())
      .map(c => ({
        name: c.name,
        count: c.count,
        avgPrice: c.count > 0 ? Math.round(c.totalPrice / c.count) : 0,
        brandCount: c.brands.size,
      }))
      .sort((a, b) => b.count - a.count);
  }, [products]);

  const storeStats = useMemo(() => {
    const map = new Map<string, { name: string; count: number; totalPrice: number }>();
    products.forEach(p => {
      p.prices?.forEach(pr => {
        const key = pr.store.name;
        if (!map.has(key)) map.set(key, { name: key, count: 0, totalPrice: 0 });
        const s = map.get(key)!;
        s.count++;
        s.totalPrice += pr.price;
      });
    });
    return Array.from(map.values())
      .map(s => ({ name: s.name, products: s.count, avgPrice: s.count > 0 ? Math.round(s.totalPrice / s.count) : 0 }))
      .sort((a, b) => b.products - a.products)
      .slice(0, 8);
  }, [products]);

  const priceDistribution = useMemo(() => {
    const buckets = [
      { name: '€0-50', min: 0, max: 50, count: 0 },
      { name: '€51-100', min: 51, max: 100, count: 0 },
      { name: '€101-150', min: 101, max: 150, count: 0 },
      { name: '€151-200', min: 151, max: 200, count: 0 },
      { name: '€200+', min: 201, max: Infinity, count: 0 },
    ];
    products.forEach(p => {
      const lp = p.lowestPrice ?? 0;
      const b = buckets.find(b => lp >= b.min && lp <= b.max);
      if (b) b.count++;
    });
    return buckets;
  }, [products]);

  const statusLabel = (count: number, total: number) =>
    count / total >= 0.15 ? 'Strong' : 'Growing';

  return (
    <div className="space-y-6">
      {/* Row 1: Brand bar + Category pie */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-semibold">Brand Performance</CardTitle>
            <CardDescription className="text-xs">Product count and average price by brand</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={brandStats} margin={{ top: 5, right: 10, left: -10, bottom: 40 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" tick={{ fontSize: 10 }} angle={-35} textAnchor="end" interval={0} />
                <YAxis yAxisId="left" tick={{ fontSize: 10 }} />
                <YAxis yAxisId="right" orientation="right" tick={{ fontSize: 10 }} />
                <Tooltip formatter={(v, n) => [n === 'avgPrice' ? `€${v}` : v, n === 'avgPrice' ? 'Avg Price' : 'Products']} />
                <Legend iconSize={8} wrapperStyle={{ fontSize: 10 }} />
                <Bar yAxisId="left" dataKey="products" name="Products" fill="#6366f1" radius={[3, 3, 0, 0]} />
                <Bar yAxisId="right" dataKey="avgPrice" name="Avg Price (€)" fill="#ec4899" radius={[3, 3, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-semibold">Category Distribution</CardTitle>
            <CardDescription className="text-xs">Products by category</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={categoryStats} dataKey="count" nameKey="name" cx="50%" cy="50%"
                  outerRadius={75} label={({ name, count }) => `${name} (${count})`}
                  labelLine={true}>
                  {categoryStats.map((_, i) => (
                    <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(v, _, p) => [v, p.payload.name]} />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Row 2: Store bar + Price distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-semibold">Store Performance</CardTitle>
            <CardDescription className="text-xs">Product availability and pricing by store</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={storeStats} layout="vertical" margin={{ top: 5, right: 50, left: 60, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" tick={{ fontSize: 10 }} />
                <YAxis dataKey="name" type="category" tick={{ fontSize: 10 }} width={70} />
                <Tooltip formatter={(v, n) => [n === 'avgPrice' ? `€${v}` : v, n === 'avgPrice' ? 'Avg Price' : 'Products']} />
                <Legend iconSize={8} wrapperStyle={{ fontSize: 10 }} />
                <Bar dataKey="products" name="Products" fill="#10b981" radius={[0, 3, 3, 0]} />
                <Bar dataKey="avgPrice" name="Avg Price (€)" fill="#f59e0b" radius={[0, 3, 3, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-semibold">Price Distribution</CardTitle>
            <CardDescription className="text-xs">Number of products by price range</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={priceDistribution} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="count" name="Products" fill="#ec4899" radius={[4, 4, 0, 0]}>
                  <LabelList dataKey="count" position="top" style={{ fontSize: 10 }} />
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Row 3: Top brands list + Category insights */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-semibold">Top Performing Brands</CardTitle>
            <CardDescription className="text-xs">Detailed brand metrics</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {brandStats.slice(0, 5).map((b, i) => (
                <div key={b.name} className="flex items-center gap-3">
                  <span className="w-5 h-5 rounded-full bg-indigo-100 text-indigo-700 text-xs font-bold flex items-center justify-center flex-shrink-0">
                    {i + 1}
                  </span>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{b.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {b.products} product{b.products !== 1 ? 's' : ''} · €{b.minPrice}–€{b.maxPrice}
                    </p>
                  </div>
                  <span className="text-sm font-semibold text-right">€{b.avgPrice}<br />
                    <span className="text-xs text-muted-foreground font-normal">avg price</span>
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-semibold">Category Insights</CardTitle>
            <CardDescription className="text-xs">Detailed category metrics</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {categoryStats.slice(0, 5).map((c, i) => (
                <div key={c.name} className="flex items-center gap-3">
                  <span className="w-5 h-5 rounded-full bg-pink-100 text-pink-700 text-xs font-bold flex items-center justify-center flex-shrink-0">
                    {i + 1}
                  </span>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{c.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {c.brandCount} brand{c.brandCount !== 1 ? 's' : ''} · {c.count} price point{c.count !== 1 ? 's' : ''}
                    </p>
                  </div>
                  <span className="text-sm font-semibold text-right">€{c.avgPrice}<br />
                    <span className="text-xs text-muted-foreground font-normal">avg</span>
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Row 4: Complete brand analysis table */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-semibold">Complete Brand Analysis</CardTitle>
          <CardDescription className="text-xs">Comprehensive brand performance metrics</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Brand</TableHead>
                <TableHead className="text-right">Products</TableHead>
                <TableHead className="text-right">Avg Price</TableHead>
                <TableHead>Price Range</TableHead>
                <TableHead className="text-right">Avg Stores/Product</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {brandStats.map(b => (
                <TableRow key={b.name}>
                  <TableCell className="font-medium">{b.name}</TableCell>
                  <TableCell className="text-right">{b.products}</TableCell>
                  <TableCell className="text-right">€{b.avgPrice}</TableCell>
                  <TableCell>€{b.minPrice}–€{b.maxPrice}</TableCell>
                  <TableCell className="text-right">{b.avgStores}</TableCell>
                  <TableCell>
                    <Badge variant={statusLabel(b.products, products.length) === 'Strong' ? 'default' : 'secondary'}
                      className={statusLabel(b.products, products.length) === 'Strong' ? 'bg-green-600' : ''}>
                      {statusLabel(b.products, products.length)}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}

// ─── Products Table ──────────────────────────────────────────────────────────
function ProductsTab({ stores, categories, brands }: {
  stores: StoreType[];
  categories: { id: string; name: string }[];
  brands: { id: string; name: string }[];
}) {
  const { data: products, isLoading, mutate } = useAdminProducts();
  const { trigger: deleteProduct, isMutating: deleting } = useDeleteProduct();

  const [search, setSearch] = useState('');
  const [addOpen, setAddOpen] = useState(false);
  const [editProduct, setEditProduct] = useState<Product | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Product | null>(null);

  const filtered = useMemo(() => {
    if (!products) return [];
    const q = search.toLowerCase();
    return products.filter(p =>
      p.name.toLowerCase().includes(q) ||
      p.brand?.name.toLowerCase().includes(q) ||
      p.category?.name.toLowerCase().includes(q)
    );
  }, [products, search]);

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteProduct(deleteTarget.id);
      await mutate();
      toast.success('Product deleted');
      setDeleteTarget(null);
    } catch {
      toast.error('Failed to delete product');
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="font-semibold">Products ({filtered.length})</p>
          <p className="text-xs text-muted-foreground">Add, edit, or remove products</p>
        </div>
        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input className="pl-8 w-56" placeholder="Search..." value={search}
              onChange={e => setSearch(e.target.value)} />
          </div>
          <Button size="sm" onClick={() => setAddOpen(true)}>
            <Plus className="h-4 w-4 mr-1" /> Add Product
          </Button>
        </div>
      </div>

      {isLoading ? (
        <div className="space-y-2">{Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-14 w-full" />)}</div>
      ) : (
        <div className="border rounded-lg overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/40">
                <TableHead className="w-16">Image</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Brand</TableHead>
                <TableHead>Category</TableHead>
                <TableHead>Price Range</TableHead>
                <TableHead className="text-center">Stores</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-12 text-muted-foreground">
                    {search ? 'No products match your search' : 'No products yet'}
                  </TableCell>
                </TableRow>
              ) : (
                filtered.map(product => (
                  <TableRow key={product.id}>
                    <TableCell>
                      {product.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name}
                          className="w-10 h-10 object-cover rounded-md" />
                      ) : (
                        <div className="w-10 h-10 bg-muted rounded-md flex items-center justify-center">
                          <Package className="h-4 w-4 text-muted-foreground" />
                        </div>
                      )}
                    </TableCell>
                    <TableCell className="font-medium max-w-[180px]">
                      <p className="truncate">{product.name}</p>
                    </TableCell>
                    <TableCell className="text-muted-foreground text-sm">
                      {product.brand?.name ?? '—'}
                    </TableCell>
                    <TableCell>
                      {product.category && (
                        <Badge variant="outline" className="text-xs">{product.category.name}</Badge>
                      )}
                    </TableCell>
                    <TableCell className="text-sm">
                      {product.lowestPrice != null && product.highestPrice != null
                        ? `€${Math.round(product.lowestPrice)}–€${Math.round(product.highestPrice)}`
                        : product.lowestPrice != null
                        ? `€${Math.round(product.lowestPrice)}`
                        : '—'}
                    </TableCell>
                    <TableCell className="text-center text-sm">{product.storeCount}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" className="h-8 w-8"
                          onClick={() => setEditProduct(product)}>
                          <Pencil className="h-3.5 w-3.5" />
                        </Button>
                        <Button variant="ghost" size="icon" className="h-8 w-8 text-red-500 hover:text-red-600"
                          onClick={() => setDeleteTarget(product)}>
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      )}

      <ProductDialog open={addOpen} onClose={() => setAddOpen(false)} product={null}
        stores={stores} categories={categories} brands={brands}
        onSaved={() => mutate()} />
      <ProductDialog open={!!editProduct} onClose={() => setEditProduct(null)} product={editProduct}
        stores={stores} categories={categories} brands={brands}
        onSaved={() => mutate()} />
      <DeleteDialog open={!!deleteTarget} onClose={() => setDeleteTarget(null)}
        label={deleteTarget?.name ?? ''} onConfirm={handleDelete} isBusy={deleting} />
    </div>
  );
}

// ─── Stores Table ────────────────────────────────────────────────────────────
function StoresTab() {
  const { data: stores, isLoading, mutate } = useStores();
  const { trigger: deleteStore, isMutating: deleting } = useDeleteStore();

  const [addOpen, setAddOpen] = useState(false);
  const [editStore, setEditStore] = useState<StoreType | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<StoreType | null>(null);

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteStore(deleteTarget.id);
      await mutate();
      toast.success('Store deleted');
      setDeleteTarget(null);
    } catch {
      toast.error('Failed to delete store');
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="font-semibold">Stores ({stores?.length ?? 0})</p>
          <p className="text-xs text-muted-foreground">Manage connected stores</p>
        </div>
        <Button size="sm" onClick={() => setAddOpen(true)}>
          <Plus className="h-4 w-4 mr-1" /> Add Store
        </Button>
      </div>

      {isLoading ? (
        <div className="space-y-2">{Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-12 w-full" />)}</div>
      ) : (
        <div className="border rounded-lg overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/40">
                <TableHead>Name</TableHead>
                <TableHead>Website</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {(!stores || stores.length === 0) ? (
                <TableRow>
                  <TableCell colSpan={3} className="text-center py-12 text-muted-foreground">No stores yet</TableCell>
                </TableRow>
              ) : (
                stores.map(store => (
                  <TableRow key={store.id}>
                    <TableCell className="font-medium">{store.name}</TableCell>
                    <TableCell>
                      <a href={store.website} target="_blank" rel="noreferrer"
                        className="text-blue-600 hover:underline text-sm flex items-center gap-1 w-fit">
                        {store.website} <ExternalLink className="h-3 w-3" />
                      </a>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" className="h-8 w-8"
                          onClick={() => setEditStore(store)}>
                          <Pencil className="h-3.5 w-3.5" />
                        </Button>
                        <Button variant="ghost" size="icon" className="h-8 w-8 text-red-500 hover:text-red-600"
                          onClick={() => setDeleteTarget(store)}>
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      )}

      <StoreDialog open={addOpen} onClose={() => setAddOpen(false)} store={null} onSaved={() => mutate()} />
      <StoreDialog open={!!editStore} onClose={() => setEditStore(null)} store={editStore} onSaved={() => mutate()} />
      <DeleteDialog open={!!deleteTarget} onClose={() => setDeleteTarget(null)}
        label={deleteTarget?.name ?? ''} onConfirm={handleDelete} isBusy={deleting} />
    </div>
  );
}

// ─── Main AdminPage ──────────────────────────────────────────────────────────
export function AdminPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const { data: stats, isLoading: statsLoading, mutate: refreshStats } = useAdminStats();
  const { data: fakerStatus, mutate: refreshFakerStatus } = useFakerStatus();
  const { trigger: runScraper, isMutating: scraperRunning } = useRunScraper();
  const { trigger: startFaker, isMutating: fakerStarting } = useStartFaker();
  const { trigger: stopFaker, isMutating: fakerStopping } = useStopFaker();
  const { data: adminProducts } = useAdminProducts();
  const { data: stores = [] } = useStores();
  const { data: categories = [] } = useCategories();
  const { data: brands = [] } = useBrands();

  const [intervalMs, setIntervalMs] = useState(3000);
  const [batchSize, setBatchSize] = useState(5);

  if (user?.role !== 'ADMIN') {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center space-y-4">
          <ShieldAlert className="h-16 w-16 text-red-500 mx-auto" />
          <h1 className="text-2xl font-bold">Access Denied</h1>
          <p className="text-muted-foreground">You need admin privileges to view this page.</p>
          <Button onClick={() => navigate('/')}>Go Home</Button>
        </div>
      </div>
    );
  }

  const handleRunScraper = async () => {
    try {
      await runScraper();
      await refreshStats();
      toast.success('Scraper triggered successfully');
    } catch {
      toast.error('Failed to trigger scraper');
    }
  };

  const handleStartFaker = async () => {
    try {
      await startFaker({ intervalMs, batchSize });
      await refreshFakerStatus();
      toast.success(`Faker started — ${batchSize} products every ${intervalMs}ms`);
    } catch {
      toast.error('Failed to start faker');
    }
  };

  const handleStopFaker = async () => {
    try {
      await stopFaker();
      await refreshFakerStatus();
      toast.success('Faker stopped');
    } catch {
      toast.error('Failed to stop faker');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate('/')}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div className="flex items-center gap-2">
            <ShieldAlert className="h-6 w-6 text-blue-600" />
            <h1 className="text-xl font-bold">Admin Dashboard</h1>
          </div>
          <Badge variant="secondary" className="ml-auto">{user.email}</Badge>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        <Tabs defaultValue="overview">
          <TabsList className="mb-6">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="analytics">Analytics</TabsTrigger>
            <TabsTrigger value="manage">Manage Data</TabsTrigger>
          </TabsList>

          {/* ── Overview ── */}
          <TabsContent value="overview" className="space-y-8">
            <section>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold">Overview</h2>
                <Button variant="outline" size="sm" onClick={() => refreshStats()} disabled={statsLoading}>
                  <RefreshCw className={`h-4 w-4 mr-2 ${statsLoading ? 'animate-spin' : ''}`} />
                  Refresh
                </Button>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                <StatCard icon={Package} label="Total Products" value={stats?.totalProducts} color="bg-blue-500" />
                <StatCard icon={Store} label="Active Stores" value={stats?.activeStores} color="bg-green-500" />
                <StatCard icon={Tag} label="Total Brands" value={stats?.totalBrands} color="bg-purple-500" />
                <StatCard icon={DollarSign} label="Total Prices" value={stats?.totalPrices} color="bg-yellow-500" />
                <StatCard icon={TrendingUp} label="Avg Price"
                  value={stats?.avgPrice !== undefined ? `€${stats.avgPrice.toFixed(2)}` : undefined}
                  color="bg-orange-500" />
                <Card>
                  <CardContent className="pt-6">
                    <p className="text-sm text-muted-foreground">Top Priced Product</p>
                    {stats ? (
                      <p className="text-sm font-medium mt-1 truncate" title={stats.topPriceProduct}>
                        {stats.topPriceProduct}
                      </p>
                    ) : (
                      <Skeleton className="h-5 w-full mt-1" />
                    )}
                  </CardContent>
                </Card>
              </div>
            </section>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Scraper</CardTitle>
                  <CardDescription>Pull fresh data from connected stores (Kith, MNML, CultureKings, Asphaltgold).</CardDescription>
                </CardHeader>
                <CardContent>
                  <Button onClick={handleRunScraper} disabled={scraperRunning} className="w-full">
                    <RefreshCw className={`h-4 w-4 mr-2 ${scraperRunning ? 'animate-spin' : ''}`} />
                    {scraperRunning ? 'Triggering…' : 'Run Scraper'}
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    Fake Data Generator
                    {fakerStatus?.generating && (
                      <Badge className="bg-green-500 text-white animate-pulse">Running</Badge>
                    )}
                  </CardTitle>
                  <CardDescription>Generate realistic clothing products at a configurable rate.</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1">
                      <Label htmlFor="intervalMs">Interval (ms)</Label>
                      <Input id="intervalMs" type="number" min={500} step={500} value={intervalMs}
                        onChange={e => setIntervalMs(Number(e.target.value))}
                        disabled={fakerStatus?.generating} />
                    </div>
                    <div className="space-y-1">
                      <Label htmlFor="batchSize">Batch Size</Label>
                      <Input id="batchSize" type="number" min={1} max={50} value={batchSize}
                        onChange={e => setBatchSize(Number(e.target.value))}
                        disabled={fakerStatus?.generating} />
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button onClick={handleStartFaker} disabled={fakerStatus?.generating || fakerStarting} className="flex-1">
                      <Play className="h-4 w-4 mr-2" />
                      {fakerStarting ? 'Starting…' : 'Start'}
                    </Button>
                    <Button variant="destructive" onClick={handleStopFaker}
                      disabled={!fakerStatus?.generating || fakerStopping} className="flex-1">
                      <Square className="h-4 w-4 mr-2" />
                      {fakerStopping ? 'Stopping…' : 'Stop'}
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* ── Analytics ── */}
          <TabsContent value="analytics">
            {!adminProducts || adminProducts.length === 0 ? (
              <div className="text-center py-20 text-muted-foreground">
                <TrendingUp className="h-12 w-12 mx-auto mb-3 opacity-30" />
                <p className="font-medium">No data yet</p>
                <p className="text-sm mt-1">Run the scraper or faker to generate products first.</p>
              </div>
            ) : (
              <AnalyticsDashboard products={adminProducts} />
            )}
          </TabsContent>

          {/* ── Manage Data ── */}
          <TabsContent value="manage">
            <div className="mb-4">
              <h2 className="text-lg font-semibold">Manage Data</h2>
              <p className="text-sm text-muted-foreground">Add, edit, or remove products and stores</p>
            </div>
            <Tabs defaultValue="products">
              <TabsList className="mb-4">
                <TabsTrigger value="products">Products</TabsTrigger>
                <TabsTrigger value="stores">Stores</TabsTrigger>
              </TabsList>
              <TabsContent value="products">
                <ProductsTab stores={stores} categories={categories} brands={brands} />
              </TabsContent>
              <TabsContent value="stores">
                <StoresTab />
              </TabsContent>
            </Tabs>
          </TabsContent>
        </Tabs>
      </main>
    </div>
  );
}
