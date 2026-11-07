import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Компонент защиты маршрутов (Protected Route).
 * Проверяет статус аутентификации пользователя в глобальном контексте.
 *
 * Если пользователь аутентифицирован:
 *   - Отображает дочерний компонент (например, страницу AI-чата).
 *
 * Если пользователь НЕ аутентифицирован:
 *   - Выполняет редирект на страницу /login.
 *   - Передает в location.state информацию об исходном маршруте и сообщение:
 *     "You must be logged in to access the chat."
 */
export default function ProtectedRoute({ children }) {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="protected-route-loading" role="status" aria-live="polite">
        <div className="spinner-orbit"></div>
        <p className="loading-text">Проверка статуса авторизации...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    console.warn(
      `[ProtectedRoute] Неаутентифицированная попытка доступа к ${location.pathname}. Перенаправление на /login.`
    );
    return (
      <Navigate
        to="/login"
        state={{
          from: location.pathname,
          message: 'You must be logged in to access the chat.',
          type: 'warning',
        }}
        replace
      />
    );
  }

  return children;
}

