'use client';

import { useEffect, useRef } from 'react';
import { usePathname, useSearchParams } from 'next/navigation';

export default function ActivityTracker() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const isFirst = useRef(true);

  useEffect(() => {
    // Skip the first render — tracker.js fires PAGE_VIEW on load already
    if (isFirst.current) {
      isFirst.current = false;
      return;
    }
    if (typeof window === 'undefined' || !window.ActivityTracker) return;
    window.ActivityTracker.track('PAGE_VIEW', {
      path: pathname,
      search: searchParams.toString() ? '?' + searchParams.toString() : '',
      referrer: document.referrer,
      title: document.title,
    });
  }, [pathname, searchParams]);

  return null;
}
