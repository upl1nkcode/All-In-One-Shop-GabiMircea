import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

const COLORS = ['#8b5cf6', '#ec4899', '#3b82f6', '#10b981', '#f59e0b', '#ef4444'];

export function StatisticsTab({ products, stores, categories, brands }: any) {
  
  // 1. Brand Performance
  const brandData = brands.map((brand: any) => {
    const brandProducts = products.filter((p: any) => p.brand?.id === brand.id);
    const avgPrice = brandProducts.reduce((sum: number, p: any) => {
      const prices = p.prices?.map((pr: any) => pr.price) || [];
      return sum + (prices.length ? prices[0] : 0);
    }, 0) / (brandProducts.length || 1);
    
    return {
      name: brand.name,
      Products: brandProducts.length,
      AvgPrice: Math.round(avgPrice),
    };
  }).filter((b: any) => b.Products > 0).sort((a: any, b: any) => b.Products - a.Products).slice(0, 6);

  // 2. Category Distribution
  const categoryData = categories.map((cat: any) => ({
    name: cat.name,
    value: products.filter((p: any) => p.category?.id === cat.id).length
  })).filter((c: any) => c.value > 0);

  // 3. Price Distribution Map
  const priceRanges = [
    { range: '€0 - €50', min: 0, max: 50, count: 0 },
    { range: '€50 - €100', min: 50, max: 100, count: 0 },
    { range: '€100 - €150', min: 100, max: 150, count: 0 },
    { range: '€150 - €200', min: 150, max: 200, count: 0 },
    { range: '€200+', min: 200, max: 9999, count: 0 },
  ];
  
  products.forEach((p: any) => {
    const prices = p.prices?.map((pr: any) => pr.price) || [];
    if(prices.length === 0) return;
    const avg = prices.reduce((a:any,b:any)=>a+b,0) / prices.length;
    for(let r of priceRanges) {
      if(avg >= r.min && avg < r.max) {
        r.count++;
        break;
      }
    }
  });

  return (
    <div className="space-y-8">
      
      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Brand Performance */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex flex-col items-center place-content-center h-[350px]">
          <div className="w-full mb-4">
            <h3 className="font-semibold text-gray-900 text-sm">Brand Performance</h3>
            <p className="text-xs text-gray-500">Product count and average price by brand</p>
          </div>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={brandData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
              <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill:'#64748b', fontSize: 12}} />
              <YAxis yAxisId="left" axisLine={false} tickLine={false} tick={{fill:'#64748b', fontSize: 12}} />
              <YAxis yAxisId="right" orientation="right" axisLine={false} tickLine={false} tick={false} />
              <Tooltip cursor={{fill: '#f8fafc'}} contentStyle={{borderRadius: '8px', border:'none', boxShadow:'0 4px 6px -1px rgb(0 0 0 / 0.1)'}} />
              <Bar yAxisId="left" dataKey="Products" fill="#8b5cf6" radius={[4, 4, 0, 0]} barSize={16} />
              <Bar yAxisId="right" dataKey="AvgPrice" fill="#ec4899" radius={[4, 4, 0, 0]} barSize={16} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Category Distribution */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex flex-col items-center place-content-center h-[350px]">
          <div className="w-full mb-4">
            <h3 className="font-semibold text-gray-900 text-sm">Category Distribution</h3>
            <p className="text-xs text-gray-500">Products by root category</p>
          </div>
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={categoryData} cx="50%" cy="50%" innerRadius={60} outerRadius={80} paddingAngle={2} dataKey="value" stroke="none">
                {categoryData.map((e:any, i:any) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Pie>
              <Tooltip contentStyle={{borderRadius: '8px', border:'none', boxShadow:'0 4px 6px -1px rgb(0 0 0 / 0.1)'}} />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Price Distribution */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex flex-col items-center place-content-center h-[350px]">
          <div className="w-full mb-4">
            <h3 className="font-semibold text-gray-900 text-sm">Price Distribution</h3>
            <p className="text-xs text-gray-500">Number of products by price range</p>
          </div>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={priceRanges} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
              <XAxis dataKey="range" axisLine={false} tickLine={false} tick={{fill:'#64748b', fontSize: 10}} />
              <YAxis axisLine={false} tickLine={false} tick={{fill:'#64748b', fontSize: 12}} />
              <Tooltip cursor={{fill: '#f8fafc'}} contentStyle={{borderRadius: '8px', border:'none', boxShadow:'0 4px 6px -1px rgb(0 0 0 / 0.1)'}} />
              <Bar dataKey="count" fill="#ec4899" radius={[4, 4, 0, 0]} barSize={32} />
            </BarChart>
          </ResponsiveContainer>
        </div>

      </div>

    </div>
  );
}
