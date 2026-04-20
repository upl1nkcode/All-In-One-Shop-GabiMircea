'use client';

import Link from 'next/link';
import { ShoppingBag, Lock, LayoutDashboard } from 'lucide-react';
import { useAuth } from '@/lib/auth';

export function Header() {
  const { isAuthenticated } = useAuth();
  return (
    <header className="sticky top-0 z-50 bg-white/80 backdrop-blur-lg border-b border-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link href="/" className="flex items-center gap-2">
            <ShoppingBag className="w-7 h-7 text-brand-600" />
            <span className="text-xl font-bold bg-gradient-to-r from-brand-600 to-purple-600 bg-clip-text text-transparent">
              AllInOne Shop
            </span>
          </Link>

          <nav className="hidden md:flex items-center gap-8">
            <Link href="/search" className="text-gray-600 hover:text-gray-900 font-medium transition-colors">
              Browse
            </Link>
            <Link href="/search?category=sneakers" className="text-gray-600 hover:text-gray-900 font-medium transition-colors">
              Sneakers
            </Link>
            <Link href="/search?category=jackets" className="text-gray-600 hover:text-gray-900 font-medium transition-colors">
              Jackets
            </Link>
          </nav>

          <div className="flex items-center gap-4">
            <Link
              href="/search"
              className="btn-secondary text-sm py-2 px-4 flex items-center gap-2"
            >
              Search
            </Link>
            {isAuthenticated ? (
              <Link
                href="/admin"
                className="btn-primary text-sm py-2 px-4 flex items-center gap-2"
              >
                <LayoutDashboard className="w-4 h-4" />
                <span className="hidden sm:inline">Admin</span>
              </Link>
            ) : (
              <Link
                href="/login"
                className="btn-primary text-sm py-2 px-4 flex items-center gap-2"
              >
                <Lock className="w-4 h-4" />
                <span className="hidden sm:inline">Login</span>
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
