import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { registerUser, loginUser } from '../services/api';

const AuthContext = createContext(null);

const STORAGE_KEY_USER = 'gh_internship_user';
const STORAGE_KEY_TOKEN = 'gh_internship_token';

/**
 * Провайдер глобального состояния аутентификации (React Context)
 */
export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem(STORAGE_KEY_USER);
      return savedUser ? JSON.parse(savedUser) : null;
    } catch (e) {
      console.warn('[AuthContext] Ошибка чтения пользователя из localStorage:', e);
      return null;
    }
  });

  const [token, setToken] = useState(() => {
    try {
      return localStorage.getItem(STORAGE_KEY_TOKEN) || null;
    } catch {
      return null;
    }
  });

  const [isLoading, setIsLoading] = useState(false);

  // Синхронизация состояния с localStorage при изменении
  useEffect(() => {
    try {
      if (currentUser) {
        localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(currentUser));
      } else {
        localStorage.removeItem(STORAGE_KEY_USER);
      }
    } catch (e) {
      console.warn('[AuthContext] Ошибка сохранения пользователя в localStorage:', e);
    }
  }, [currentUser]);

  useEffect(() => {
    try {
      if (token) {
        localStorage.setItem(STORAGE_KEY_TOKEN, token);
      } else {
        localStorage.removeItem(STORAGE_KEY_TOKEN);
      }
    } catch (e) {
      console.warn('[AuthContext] Ошибка сохранения токена в localStorage:', e);
    }
  }, [token]);

  /**
   * Выполнение входа пользователя в систему
   */
  const login = useCallback(async ({ username, password, rememberMe = true }) => {
    setIsLoading(true);
    try {
      console.log(`[AuthContext] Попытка авторизации пользователя: ${username}`);
      const result = await loginUser({ username, password });

      const user = {
        username: result.username || username,
        id: result.id || 'N/A',
        loginTime: new Date().toLocaleTimeString(),
      };

      const userToken = result.token || `session-${Date.now()}`;

      setCurrentUser(user);
      setToken(userToken);

      if (rememberMe) {
        localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(user));
        localStorage.setItem(STORAGE_KEY_TOKEN, userToken);
      }

      console.log(`[AuthContext] Пользователь ${user.username} успешно авторизован`);
      return { success: true, user, token: userToken, data: result };
    } catch (error) {
      console.error('[AuthContext] Ошибка авторизации:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Регистрация нового пользователя
   */
  const register = useCallback(async ({ username, password }) => {
    setIsLoading(true);
    try {
      console.log(`[AuthContext] Попытка регистрации пользователя: ${username}`);
      const result = await registerUser({ username, password });
      console.log('[AuthContext] Пользователь успешно зарегистрирован на бэкенде');
      return { success: true, data: result };
    } catch (error) {
      console.error('[AuthContext] Ошибка при регистрации:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Завершение сеанса пользователя (Logout)
   */
  const logout = useCallback(() => {
    console.log('[AuthContext] Завершение сеанса пользователя');
    setCurrentUser(null);
    setToken(null);
    try {
      localStorage.removeItem(STORAGE_KEY_USER);
      localStorage.removeItem(STORAGE_KEY_TOKEN);
    } catch (e) {
      console.warn('[AuthContext] Ошибка очистки localStorage:', e);
    }
  }, []);

  const value = {
    currentUser,
    token,
    isAuthenticated: Boolean(currentUser),
    isLoading,
    login,
    register,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Хук для использования контекста аутентификации
 */
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
