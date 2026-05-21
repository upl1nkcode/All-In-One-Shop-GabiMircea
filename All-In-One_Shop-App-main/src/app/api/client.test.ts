import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { setAuthToken, getAuthToken, isAuthenticated } from './client';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] ?? null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { store = {}; },
  };
})();

Object.defineProperty(global, 'localStorage', { value: localStorageMock });

describe('Auth token management', () => {
  beforeEach(() => {
    localStorageMock.clear();
    setAuthToken(null);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('setAuthToken stores token in localStorage', () => {
    setAuthToken('my.jwt.token');
    expect(localStorage.getItem('auth_token')).toBe('my.jwt.token');
  });

  it('setAuthToken(null) removes token from localStorage', () => {
    setAuthToken('some.token');
    setAuthToken(null);
    expect(localStorage.getItem('auth_token')).toBeNull();
  });

  it('getAuthToken returns stored token', () => {
    setAuthToken('returned.token');
    expect(getAuthToken()).toBe('returned.token');
  });

  it('getAuthToken returns null when no token set', () => {
    expect(getAuthToken()).toBeNull();
  });

  it('isAuthenticated returns true when token is set', () => {
    setAuthToken('valid.token');
    expect(isAuthenticated()).toBe(true);
  });

  it('isAuthenticated returns false when no token', () => {
    expect(isAuthenticated()).toBe(false);
  });

  it('isAuthenticated returns false after token cleared', () => {
    setAuthToken('token');
    setAuthToken(null);
    expect(isAuthenticated()).toBe(false);
  });
});

describe('authApi functions', () => {
  beforeEach(() => {
    localStorageMock.clear();
    setAuthToken(null);
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('authApi.login stores token on success', async () => {
    const { authApi } = await import('./client');

    const mockResponse = {
      ok: true,
      json: async () => ({
        success: true,
        data: {
          token: 'login.jwt.token',
          tokenType: 'Bearer',
          user: { id: '123', email: 'user@test.com', role: 'USER' },
        },
      }),
    };
    vi.mocked(fetch).mockResolvedValueOnce(mockResponse as unknown as Response);

    const result = await authApi.login({ email: 'user@test.com', password: 'password123' });

    expect(result.success).toBe(true);
    expect(getAuthToken()).toBe('login.jwt.token');
  });

  it('authApi.register stores token on success', async () => {
    const { authApi } = await import('./client');

    const mockResponse = {
      ok: true,
      json: async () => ({
        success: true,
        data: {
          token: 'register.jwt.token',
          tokenType: 'Bearer',
          user: { id: '456', email: 'new@test.com', role: 'USER' },
        },
      }),
    };
    vi.mocked(fetch).mockResolvedValueOnce(mockResponse as unknown as Response);

    await authApi.register({
      email: 'new@test.com',
      password: 'password123',
      firstName: 'John',
      lastName: 'Doe',
    });

    expect(getAuthToken()).toBe('register.jwt.token');
  });

  it('authApi.logout clears auth token', async () => {
    setAuthToken('existing.token');
    const { authApi } = await import('./client');

    const mockResponse = {
      ok: true,
      json: async () => ({ success: true, data: null }),
    };
    vi.mocked(fetch).mockResolvedValueOnce(mockResponse as unknown as Response);

    await authApi.logout();

    expect(getAuthToken()).toBeNull();
    expect(isAuthenticated()).toBe(false);
  });

  it('authApi.logout clears token even when request fails', async () => {
    setAuthToken('existing.token');
    const { authApi } = await import('./client');

    vi.mocked(fetch).mockRejectedValueOnce(new Error('Network error'));

    await authApi.logout();

    expect(getAuthToken()).toBeNull();
  });
});
