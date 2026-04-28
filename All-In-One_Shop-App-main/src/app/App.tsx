import { RouterProvider } from 'react-router';
import { SWRConfig } from 'swr';
import { router } from './routes';
import { AuthProvider } from './context/AuthContext';
import { NetworkProvider } from './context/NetworkContext';
import { Toaster } from './components/ui/sonner';

export default function App() {
  return (
    <SWRConfig
      value={{
        revalidateOnFocus: false,
        shouldRetryOnError: false,
      }}
    >
      <NetworkProvider>
        <AuthProvider>
          <RouterProvider router={router} />
          <Toaster position="top-right" />
        </AuthProvider>
      </NetworkProvider>
    </SWRConfig>
  );
}

