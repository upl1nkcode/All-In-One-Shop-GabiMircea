import Link from 'next/link';
import { ShoppingBag } from 'lucide-react';

export function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-400 py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div className="col-span-2 md:col-span-1">
            <Link href="/" className="flex items-center gap-2 mb-4">
              <ShoppingBag className="w-6 h-6 text-brand-400" />
              <span className="text-lg font-bold text-white">AllInOne Shop</span>
            </Link>
            <p className="text-sm">
              Compare fashion prices from multiple stores. Find the best deals, always.
            </p>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-4">Categories</h4>
            <ul className="space-y-2 text-sm">
              <li><Link href="/search?category=sneakers" className="hover:text-white transition-colors">Sneakers</Link></li>
              <li><Link href="/search?category=hoodies" className="hover:text-white transition-colors">Hoodies</Link></li>
              <li><Link href="/search?category=jackets" className="hover:text-white transition-colors">Jackets</Link></li>
              <li><Link href="/search?category=pants" className="hover:text-white transition-colors">Pants</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-4">Stores</h4>
            <ul className="space-y-2 text-sm">
              <li><span>Zalando</span></li>
              <li><span>Nike</span></li>
              <li><span>ASOS</span></li>
              <li><span>H&M</span></li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-4">Legal</h4>
            <ul className="space-y-2 text-sm">
              <li><Link href="#" className="hover:text-white transition-colors">Privacy Policy</Link></li>
              <li><Link href="#" className="hover:text-white transition-colors">Terms of Service</Link></li>
              <li><Link href="/login" className="hover:text-white transition-colors">Admin</Link></li>
            </ul>
          </div>
        </div>
        <div className="border-t border-gray-800 mt-10 pt-8 text-center text-sm">
          © {new Date().getFullYear()} AllInOne Shop. All rights reserved.
        </div>
      </div>
    </footer>
  );
}
