import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { authApi, setAuthToken, getAuthToken } from '../api/client';
import type { User, LoginRequest, RegisterRequest } from '../api/types';
import { useIdleTimer } from '../hooks/useIdleTimer';
import { toast } from 'sonner';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check for existing token on mount
    const token = getAuthToken();
    if (token) {
      authApi.getCurrentUser()
        .then(response => {
          if (response.success) {
            setUser(response.data);
          } else {
            setAuthToken(null);
          }
        })
        .catch(() => {
          setAuthToken(null);
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    if (response.success) {
      setUser(response.data.user);
    } else {
      throw new Error(response.message || 'Login failed');
    }
  };

  const register = async (data: RegisterRequest) => {
    const response = await authApi.register(data);
    if (response.success) {
      setUser(response.data.user);
    } else {
      throw new Error(response.message || 'Registration failed');
    }
  };

  const logout = useCallback(() => {
    authApi.logout();
    setUser(null);
  }, []);

  const handleIdle = useCallback(() => {
    logout();
    toast.info('You have been logged out due to inactivity.');
  }, [logout]);

  const handleIdleWarning = useCallback(() => {
    toast.warning('You will be logged out in 5 minutes due to inactivity.', {
      duration: 10000,
    });
  }, []);

  useIdleTimer({
    onIdle: handleIdle,
    onWarning: handleIdleWarning,
    enabled: !!user,
  });

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
