import React from 'react';
import { render, screen, act, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach, afterEach } from 'vitest';
import { AuthProvider, useAuth } from './AuthContext.jsx';

function AuthTestHarness() {
  const { currentUser, token, isAuthenticated, login, logout } = useAuth();
  return (
    <div>
      <span data-testid="auth-status">{isAuthenticated ? 'authenticated' : 'anonymous'}</span>
      <span data-testid="auth-username">{currentUser?.username || 'none'}</span>
      <span data-testid="auth-token">{token || 'none'}</span>
      <button
        type="button"
        onClick={() => login({ username: 'carol', password: 'CarolPassword123!', rememberMe: true })}
      >
        Trigger Login
      </button>
      <button type="button" onClick={logout}>
        Trigger Logout
      </button>
    </div>
  );
}

describe('AuthContext Unit & Integration Tests', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
  });

  test('успешный login сохраняет реальный JWT-токен с бэкенда и обновляет состояние isAuthenticated', async () => {
    const user = userEvent.setup();

    const mockFetch = vi.fn().mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({
        message: 'Login successful',
        token: 'real.backend.jwt.token.777',
        username: 'carol',
        id: 55,
      }),
    });
    vi.stubGlobal('fetch', mockFetch);

    render(
      <AuthProvider>
        <AuthTestHarness />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status')).toHaveTextContent('anonymous');
    expect(screen.getByTestId('auth-token')).toHaveTextContent('none');

    await user.click(screen.getByRole('button', { name: 'Trigger Login' }));

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('authenticated');
    });

    expect(screen.getByTestId('auth-username')).toHaveTextContent('carol');
    expect(screen.getByTestId('auth-token')).toHaveTextContent('real.backend.jwt.token.777');
    expect(localStorage.getItem('gh_internship_token')).toBe('real.backend.jwt.token.777');
    expect(JSON.parse(localStorage.getItem('gh_internship_user'))).toMatchObject({
      username: 'carol',
      id: 55,
    });
  });

  test('logout сбрасывает состояние аутентификации и очищает токен из localStorage', async () => {
    const user = userEvent.setup();
    localStorage.setItem('gh_internship_user', JSON.stringify({ username: 'carol', id: 55 }));
    localStorage.setItem('gh_internship_token', 'real.backend.jwt.token.777');

    render(
      <AuthProvider>
        <AuthTestHarness />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status')).toHaveTextContent('authenticated');

    await act(async () => {
      await user.click(screen.getByRole('button', { name: 'Trigger Logout' }));
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('anonymous');
    expect(screen.getByTestId('auth-token')).toHaveTextContent('none');
    expect(localStorage.getItem('gh_internship_token')).toBeNull();
    expect(localStorage.getItem('gh_internship_user')).toBeNull();
  });
});

