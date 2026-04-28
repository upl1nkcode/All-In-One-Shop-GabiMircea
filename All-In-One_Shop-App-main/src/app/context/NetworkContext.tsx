import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { syncPendingOperations, getPendingCount } from '../api/offlineStorage';
import { API_BASE_URL, getAuthToken } from '../api/client';
import { toast } from 'sonner';

interface NetworkContextType {
  isOnline: boolean;
  pendingOps: number;
}

const NetworkContext = createContext<NetworkContextType>({ isOnline: true, pendingOps: 0 });

export function NetworkProvider({ children }: { children: ReactNode }) {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [pendingOps, setPendingOps] = useState(getPendingCount());

  const handleSync = useCallback(async () => {
    const count = getPendingCount();
    if (count === 0) return;

    const { synced, failed } = await syncPendingOperations(API_BASE_URL, getAuthToken());
    setPendingOps(getPendingCount());

    if (synced > 0) {
      toast.success(`Synced ${synced} offline operation${synced > 1 ? 's' : ''}`);
    }
    if (failed > 0) {
      toast.error(`${failed} operation${failed > 1 ? 's' : ''} failed to sync`);
    }
  }, []);

  useEffect(() => {
    const goOnline = () => {
      setIsOnline(true);
      toast.success('You are back online');
      // Auto-sync queued operations
      handleSync();
    };

    const goOffline = () => {
      setIsOnline(false);
      toast.warning('You are offline — changes will be saved locally');
    };

    window.addEventListener('online', goOnline);
    window.addEventListener('offline', goOffline);

    return () => {
      window.removeEventListener('online', goOnline);
      window.removeEventListener('offline', goOffline);
    };
  }, [handleSync]);

  // Periodically update pending count
  useEffect(() => {
    const interval = setInterval(() => {
      setPendingOps(getPendingCount());
    }, 3000);
    return () => clearInterval(interval);
  }, []);

  return (
    <NetworkContext.Provider value={{ isOnline, pendingOps }}>
      {/* Offline banner */}
      {!isOnline && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            zIndex: 9999,
            background: 'linear-gradient(135deg, #f97316, #ef4444)',
            color: 'white',
            textAlign: 'center',
            padding: '8px 16px',
            fontSize: '14px',
            fontWeight: 500,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
          }}
        >
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="1" y1="1" x2="23" y2="23" />
            <path d="M16.72 11.06A10.94 10.94 0 0 1 19 12.55" />
            <path d="M5 12.55a10.94 10.94 0 0 1 5.17-2.39" />
            <path d="M10.71 5.05A16 16 0 0 1 22.56 9" />
            <path d="M1.42 9a15.91 15.91 0 0 1 4.7-2.88" />
            <path d="M8.53 16.11a6 6 0 0 1 6.95 0" />
            <line x1="12" y1="20" x2="12.01" y2="20" />
          </svg>
          You are offline
          {pendingOps > 0 && ` · ${pendingOps} pending operation${pendingOps > 1 ? 's' : ''}`}
        </div>
      )}
      {children}
    </NetworkContext.Provider>
  );
}

export function useNetwork() {
  return useContext(NetworkContext);
}
