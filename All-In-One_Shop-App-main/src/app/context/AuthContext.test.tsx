import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { AuthProvider, useAuth } from './AuthContext';
import * as clientModule from '../api/client';

// Mock the API client module
vi.mock('../api/client', async (importOriginal) => {
  const actual = await importOriginal<typeof clientModule>();
  return {
    ...actual,
    authApi: {
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
      getCurrentUser: vi.fn(),
    },
    setAuthToken: vi.fn(),
    getAuthToken: vi.fn(() => null),
    isAuthenticated: vi.fn(() => false),
  };
});

const mockedAuthApi = vi.mocked(clientModule.authApi);
const mockedGetAuthToken = vi.mocked(clientModule.getAuthToken);

// Helper: a test consumer component
function TestConsumer() {
  const { user, isAuthenticated, login, register, logout, isLoading } = useAuth();
  return (
    <div>
      <div data-testid="loading">{isLoading ? 'loading' : 'ready'}</div>
      <div data-testid="authenticated">{isAuthenticated ? 'yes' : 'no'}</div>
      <div data-testid="email">{user?.email ?? 'none'}</div>
      <button onClick={() => login({ email: 'a@b.com', password: 'pass1234' })}>Login</button>
      <button onClick={() => register({ email: 'n@b.com', password: 'pass1234', firstName: 'N', lastName: 'B' })}>Register</button>
      <button onClick={logout}>Logout</button>
    </div>
  );
}

function renderWithAuth() {
  return render(
    <AuthProvider>
      <TestConsumer />
    </AuthProvider>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Default: no stored token
    mockedGetAuthToken.mockReturnValue(null);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('starts unauthenticated when no token in storage', async () => {
    mockedGetAuthToken.mockReturnValue(null);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('ready');
    });
    expect(screen.getByTestId('authenticated').textContent).toBe('no');
    expect(screen.getByTestId('email').textContent).toBe('none');
  });

  it('restores session from stored token on mount', async () => {
    mockedGetAuthToken.mockReturnValue('stored.token');
    mockedAuthApi.getCurrentUser.mockResolvedValueOnce({
      success: true,
      data: { id: '1', email: 'stored@test.com', role: 'USER' },
    });

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('yes');
      expect(screen.getByTestId('email').textContent).toBe('stored@test.com');
    });
  });

  it('clears token when getCurrentUser fails on mount', async () => {
    mockedGetAuthToken.mockReturnValue('invalid.token');
    mockedAuthApi.getCurrentUser.mockRejectedValueOnce(new Error('Unauthorized'));

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('ready');
    });
    expect(screen.getByTestId('authenticated').textContent).toBe('no');
  });

  it('login updates authenticated state on success', async () => {
    const user = userEvent.setup();
    mockedAuthApi.login.mockResolvedValueOnce({
      success: true,
      data: {
        token: 'new.token',
        tokenType: 'Bearer',
        user: { id: '2', email: 'a@b.com', role: 'USER' },
      },
    });

    renderWithAuth();
    await waitFor(() => expect(screen.getByTestId('loading').textContent).toBe('ready'));

    await act(async () => {
      await user.click(screen.getByText('Login'));
    });

    expect(screen.getByTestId('authenticated').textContent).toBe('yes');
    expect(screen.getByTestId('email').textContent).toBe('a@b.com');
  });

  it('login throws on failure', async () => {
    const user = userEvent.setup();
    mockedAuthApi.login.mockResolvedValueOnce({
      success: false,
      message: 'Invalid credentials',
      data: null as unknown as never,
    });

    let errorCaught = false;
    function FailingConsumer() {
      const { login } = useAuth();
      return (
        <button onClick={async () => {
          try {
            await login({ email: 'x@y.com', password: 'wrong' });
          } catch {
            errorCaught = true;
          }
        }}>TryLogin</button>
      );
    }

    render(
      <AuthProvider>
        <FailingConsumer />
      </AuthProvider>
    );
    await waitFor(() => {});

    await act(async () => {
      await user.click(screen.getByText('TryLogin'));
    });

    expect(errorCaught).toBe(true);
  });

  it('register sets user on success', async () => {
    const user = userEvent.setup();
    mockedAuthApi.register.mockResolvedValueOnce({
      success: true,
      data: {
        token: 'reg.token',
        tokenType: 'Bearer',
        user: { id: '3', email: 'n@b.com', role: 'USER' },
      },
    });

    renderWithAuth();
    await waitFor(() => expect(screen.getByTestId('loading').textContent).toBe('ready'));

    await act(async () => {
      await user.click(screen.getByText('Register'));
    });

    expect(screen.getByTestId('authenticated').textContent).toBe('yes');
    expect(screen.getByTestId('email').textContent).toBe('n@b.com');
  });

  it('logout clears user state', async () => {
    const user = userEvent.setup();
    // Set up logged-in state
    mockedGetAuthToken.mockReturnValue('existing.token');
    mockedAuthApi.getCurrentUser.mockResolvedValueOnce({
      success: true,
      data: { id: '4', email: 'loggedIn@test.com', role: 'USER' },
    });
    mockedAuthApi.logout.mockResolvedValueOnce(undefined as unknown as never);

    renderWithAuth();
    await waitFor(() => expect(screen.getByTestId('authenticated').textContent).toBe('yes'));

    await act(async () => {
      await user.click(screen.getByText('Logout'));
    });

    expect(screen.getByTestId('authenticated').textContent).toBe('no');
    expect(screen.getByTestId('email').textContent).toBe('none');
  });

  it('useAuth throws outside provider', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    function Unprotected() {
      useAuth();
      return null;
    }

    expect(() => render(<Unprotected />)).toThrow('useAuth must be used within an AuthProvider');
    consoleError.mockRestore();
  });
});
