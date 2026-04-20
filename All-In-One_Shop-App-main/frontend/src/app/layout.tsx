import type { Metadata } from 'next';
import Script from 'next/script';
import { Suspense } from 'react';
import { Toaster } from 'sonner';
import { AuthProvider } from '@/lib/auth';
import ActivityTracker from '@/components/ActivityTracker';
import './globals.css';

export const metadata: Metadata = {
  title: 'AllInOne Shop — Compare Fashion Prices',
  description: 'Find the best deals on fashion. Compare prices from Zalando, Nike, ASOS, H&M and more — all in one place.',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          {children}
          <Toaster position="top-right" richColors />
          <Suspense>
            <ActivityTracker />
          </Suspense>
        </AuthProvider>
        <Script src="http://localhost:4000/tracker.js" strategy="afterInteractive" />
      </body>
    </html>
  );
}
