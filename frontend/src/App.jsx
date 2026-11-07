import React, { useState, useEffect, useCallback, useRef } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation, useNavigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import RegisterForm from './components/RegisterForm';
import LoginForm from './components/LoginForm';
import SuccessPage from './components/SuccessPage';
import Dashboard from './components/Dashboard';
import Chat from './components/Chat';
import ProtectedRoute from './components/ProtectedRoute';
import Notification from './components/Notification';
import { AuthProvider, useAuth } from './context/AuthContext';
import { checkBackendHealth } from './services/api';
import './App.css';

/**
 * Внутренний компонент приложения, использующий хуки маршрутизации и контекст аутентификации
 */
function AppContent() {
  const { currentUser, login, register, logout, isLoading: authLoading } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const [isLoading, setIsLoading] = useState(false);
  const [successData, setSuccessData] = useState(() => {
    try {
      const params = new URLSearchParams(window.location.search);
      const registered = params.get('registered') || params.get('success');
      const userParam = params.get('username');
      const msgParam = params.get('message');
      if (registered === 'true' || userParam) {
        const username = userParam || 'Новый пользователь';
        const message = msgParam || `Регистрация прошла успешно! Добро пожаловать, ${username}!`;
        return { username, message };
      }
    } catch {
      // Игнорируем
    }
    return null;
  });

  const [backendStatus, setBackendStatus] = useState({ online: false, message: 'Проверка...' });

  const [notification, setNotification] = useState(() => {
    try {
      const params = new URLSearchParams(window.location.search);
      const registered = params.get('registered') || params.get('success');
      const userParam = params.get('username');
      const msgParam = params.get('message');
      if (registered === 'true' || userParam) {
        const username = userParam || 'Новый пользователь';
        const message = msgParam || `Регистрация прошла успешно! Добро пожаловать, ${username}!`;
        return {
          type: 'success',
          title: 'Регистрация успешна!',
          message,
        };
      }
    } catch {
      // Игнорируем
    }
    return null;
  });

  const prevLocationStateRef = useRef(null);

  // Очистка URL параметров после обработки внешнего редиректа
  useEffect(() => {
    try {
      const params = new URLSearchParams(window.location.search);
      if (params.get('registered') || params.get('username')) {
        window.history.replaceState({}, document.title, window.location.pathname);
      }
    } catch {
      // Игнорируем
    }
  }, []);

  // Перехват сообщений от ProtectedRoute (например, "You must be logged in to access the chat.")
  useEffect(() => {
    if (location.state?.message && location.state !== prevLocationStateRef.current) {
      prevLocationStateRef.current = location.state;
      console.warn(`[App] Получено сообщение редиректа: "${location.state.message}"`);
      const timer = setTimeout(() => {
        setNotification({
          type: location.state.type || 'warning',
          title: location.state.title || 'Доступ ограничен',
          message: location.state.message,
        });
      }, 0);
      return () => clearTimeout(timer);
    }
  }, [location.state]);

  // Автоматическое скрытие уведомления через 6 секунд
  useEffect(() => {
    if (!notification) return;
    const timer = setTimeout(() => {
      setNotification(null);
    }, 6000);
    return () => clearTimeout(timer);
  }, [notification]);

  // Проверка соединения со Spring Boot бэкендом
  const verifyBackend = useCallback(async () => {
    const status = await checkBackendHealth();
    setBackendStatus(status);
  }, []);

  useEffect(() => {
    let isMounted = true;
    checkBackendHealth().then((status) => {
      if (isMounted) setBackendStatus(status);
    });

    const interval = setInterval(async () => {
      const status = await checkBackendHealth();
      if (isMounted) setBackendStatus(status);
    }, 15000);

    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, []);

  // Обработка регистрации через AuthContext
  const handleRegister = async ({ username, password }) => {
    setIsLoading(true);
    setNotification(null);
    try {
      const result = await register({ username, password });
      const registeredUsername = result.data?.username || username;
      const successMsg = result.data?.message || `Пользователь "${registeredUsername}" успешно зарегистрирован!`;

      setSuccessData({
        username: registeredUsername,
        message: successMsg,
      });

      setNotification({
        type: 'success',
        title: 'Регистрация успешна!',
        message: successMsg,
      });

      navigate('/success', {
        state: { username: registeredUsername, message: successMsg },
      });

      return { success: true, data: result.data };
    } catch (err) {
      console.error('[App] Ошибка при регистрации:', err);
      const validationErrors = err.validationErrors || err.details || null;
      let title = 'Ошибка регистрации';
      if (err.status === 400) {
        title = 'Ошибка валидации данных (400)';
      } else if (err.status === 409) {
        title = 'Конфликт: пользователь уже существует (409)';
      }

      setNotification({
        type: 'error',
        title,
        message: err.message,
        details: validationErrors,
      });

      return {
        success: false,
        error: err,
        status: err.status,
        message: err.message,
        validationErrors,
      };
    } finally {
      setIsLoading(false);
    }
  };

  // Обработка входа через AuthContext
  const handleLogin = async ({ username, password, rememberMe }) => {
    setIsLoading(true);
    setNotification(null);
    try {
      const result = await login({ username, password, rememberMe });
      setNotification({
        type: 'success',
        title: 'Успешная авторизация',
        message: `Добро пожаловать в систему, ${result.user?.username}!`,
      });
      return { success: true, data: result.data };
    } catch (err) {
      console.error('[App] Ошибка при входе:', err);
      const validationErrors = err.validationErrors || err.details || null;
      let title = 'Ошибка авторизации';
      if (err.status === 400) {
        title = 'Ошибка валидации данных (400)';
      } else if (err.status === 401) {
        title = 'Неверные учетные данные (401)';
      }

      setNotification({
        type: 'error',
        title,
        message: err.message || 'Неверное имя пользователя или пароль',
        details: validationErrors,
      });

      return {
        success: false,
        error: err,
        status: err.status,
        message: err.message || 'Неверное имя пользователя или пароль',
        validationErrors,
      };
    } finally {
      setIsLoading(false);
    }
  };

  // Обработка выхода из системы
  const handleLogout = () => {
    logout();
    setNotification({
      type: 'info',
      title: 'Сеанс завершен',
      message: 'Вы успешно вышли из учетной записи.',
    });
    navigate('/login');
  };

  return (
    <div className="app-layout">
      {/* Фоновые градиентные декоративные сферы */}
      <div className="bg-glow bg-glow-1" aria-hidden="true"></div>
      <div className="bg-glow bg-glow-2" aria-hidden="true"></div>
      <div className="bg-glow bg-glow-3" aria-hidden="true"></div>

      {/* Верхняя панель навигации */}
      <Navbar
        backendStatus={backendStatus}
        onRefreshBackend={verifyBackend}
        currentUser={currentUser}
        onLogout={handleLogout}
      />

      {/* Всплывающее тост-уведомление */}
      <div className="toast-portal">
        <Notification
          notification={notification}
          onClose={() => setNotification(null)}
        />
      </div>

      {/* Основная рабочая область с защищенной и открытой маршрутизацией */}
      <main className="main-content">
        <Routes>
          {/* Главная страница: перенаправляет в зависимости от авторизации */}
          <Route
            path="/"
            element={<Navigate to={currentUser ? '/chat' : '/login'} replace />}
          />

          {/* Публичная форма входа */}
          <Route
            path="/login"
            element={
              <div className="auth-wrapper">
                <div className="auth-container">
                  <LoginForm
                    onLogin={handleLogin}
                    onSwitchToRegister={() => navigate('/register')}
                    isLoading={isLoading || authLoading}
                  />
                </div>
              </div>
            }
          />

          {/* Публичная форма регистрации */}
          <Route
            path="/register"
            element={
              <div className="auth-wrapper">
                <div className="auth-container">
                  <RegisterForm
                    onRegister={handleRegister}
                    onSwitchToLogin={() => navigate('/login')}
                    isLoading={isLoading || authLoading}
                  />
                </div>
              </div>
            }
          />

          {/* Страница успеха регистрации */}
          <Route
            path="/success"
            element={
              <div className="auth-wrapper">
                <div className="auth-container">
                  <SuccessPage
                    username={successData?.username || location.state?.username}
                    message={successData?.message || location.state?.message}
                    onSwitchToLogin={() => navigate('/login')}
                    onGoRegisterAnother={() => navigate('/register')}
                  />
                </div>
              </div>
            }
          />

          {/* Защищенный маршрут: Личный кабинет */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard
                  user={currentUser}
                  onLogout={handleLogout}
                  backendStatus={backendStatus}
                  onRefreshBackend={verifyBackend}
                />
              </ProtectedRoute>
            }
          />

          {/* Защищенный маршрут: AI-чат */}
          <Route
            path="/chat"
            element={
              <ProtectedRoute>
                <Chat />
              </ProtectedRoute>
            }
          />

          {/* Перенаправление для любых неизвестных маршрутов */}
          <Route
            path="*"
            element={<Navigate to={currentUser ? '/chat' : '/login'} replace />}
          />
        </Routes>
      </main>

      {/* Футер */}
      <footer className="footer-container">
        <p className="footer-text">
          GHInternship 2026 &bull; Spring Boot REST API & React Integration
        </p>
      </footer>
    </div>
  );
}

/**
 * Корневой компонент приложения, оборачивающий дерево в Router и AuthProvider
 */
export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}
