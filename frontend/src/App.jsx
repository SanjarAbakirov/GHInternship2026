import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import RegisterForm from './components/RegisterForm';
import LoginForm from './components/LoginForm';
import SuccessPage from './components/SuccessPage';
import Dashboard from './components/Dashboard';
import Notification from './components/Notification';
import { registerUser, loginUser, checkBackendHealth } from './services/api';
import './App.css';

export default function App() {
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
      // Игнорируем ошибки парсинга URL
    }
    return null;
  });

  const [activeTab, setActiveTab] = useState(() => {
    try {
      const params = new URLSearchParams(window.location.search);
      if (params.get('registered') === 'true' || params.get('username')) {
        return 'success';
      }
    } catch {
      // Игнорируем
    }
    return 'login';
  });

  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const saved = localStorage.getItem('gh_internship_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [isLoading, setIsLoading] = useState(false);
  const [notification, setNotification] = useState(() => {
    try {
      const params = new URLSearchParams(window.location.search);
      if (params.get('registered') === 'true' || params.get('username')) {
        const username = params.get('username') || 'Новый пользователь';
        const message = params.get('message') || `Регистрация прошла успешно! Добро пожаловать, ${username}!`;
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
  const [backendStatus, setBackendStatus] = useState({ online: false, message: 'Проверка...' });

  // Автоматическая очистка параметров URL после обработки внешнего редиректа
  useEffect(() => {
    if (successData) {
      try {
        window.history.replaceState({}, document.title, window.location.pathname);
      } catch {
        // Игнорируем
      }
    }
  }, [successData]);

  // Автоматическое скрытие уведомления через 6 секунд
  useEffect(() => {
    if (!notification) return;
    const timer = setTimeout(() => {
      setNotification(null);
    }, 6000);
    return () => clearTimeout(timer);
  }, [notification]);

  // Проверка соединения с бэкендом
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

  // Обработка регистрации
  const handleRegister = async ({ username, password }) => {
    setIsLoading(true);
    setNotification(null);
    try {
      const result = await registerUser({ username, password });
      const registeredUsername = result.username || username;
      const successMsg = result.message || `Пользователь "${registeredUsername}" успешно зарегистрирован!`;

      // Сохраняем данные для отображения страницы успеха
      setSuccessData({
        username: registeredUsername,
        message: successMsg,
      });

      // Перенаправляем пользователя на Success Page (паттерн Post/Redirect/Get)
      setActiveTab('success');

      setNotification({
        type: 'success',
        title: 'Регистрация успешна!',
        message: successMsg,
      });

      return { success: true, data: result };
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

  // Обработка входа
  const handleLogin = async ({ username, password, rememberMe }) => {
    setIsLoading(true);
    setNotification(null);
    try {
      const result = await loginUser({ username, password });
      const user = {
        username: result.username || username,
        id: result.id || 'N/A',
        loginTime: new Date().toLocaleTimeString(),
      };
      setCurrentUser(user);
      if (rememberMe) {
        localStorage.setItem('gh_internship_user', JSON.stringify(user));
      }
      setNotification({
        type: 'success',
        title: 'Успешная авторизация',
        message: `Добро пожаловать в систему, ${user.username}!`,
      });
      return { success: true, data: result };
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

  // Выход из системы
  const handleLogout = () => {
    setCurrentUser(null);
    localStorage.removeItem('gh_internship_user');
    setActiveTab('login');
    setNotification({
      type: 'info',
      title: 'Сеанс завершен',
      message: 'Вы успешно вышли из учетной записи.',
    });
  };

  return (
    <div className="app-layout">
      {/* Фоновые градиентные декоративные сферы */}
      <div className="bg-glow bg-glow-1" aria-hidden="true"></div>
      <div className="bg-glow bg-glow-2" aria-hidden="true"></div>
      <div className="bg-glow bg-glow-3" aria-hidden="true"></div>

      {/* Верхняя панель навигации */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={(tab) => {
          setNotification(null);
          setActiveTab(tab);
        }}
        backendStatus={backendStatus}
        onRefreshBackend={verifyBackend}
        currentUser={currentUser}
        onLogout={handleLogout}
      />

      {/* Всплывающее уведомление */}
      <div className="toast-portal">
        <Notification
          notification={notification}
          onClose={() => setNotification(null)}
        />
      </div>

      {/* Основная рабочая область */}
      <main className="main-content">
        {currentUser ? (
          <Dashboard
            user={currentUser}
            onLogout={handleLogout}
            backendStatus={backendStatus}
            onRefreshBackend={verifyBackend}
          />
        ) : (
          <div className="auth-wrapper">
            <div className="auth-container">
              {activeTab === 'success' ? (
                <SuccessPage
                  username={successData?.username}
                  message={successData?.message}
                  onSwitchToLogin={() => {
                    setNotification(null);
                    setActiveTab('login');
                  }}
                  onGoRegisterAnother={() => {
                    setNotification(null);
                    setSuccessData(null);
                    setActiveTab('register');
                  }}
                />
              ) : activeTab === 'register' ? (
                <RegisterForm
                  onRegister={handleRegister}
                  onSwitchToLogin={() => {
                    setNotification(null);
                    setActiveTab('login');
                  }}
                  isLoading={isLoading}
                />
              ) : (
                <LoginForm
                  onLogin={handleLogin}
                  onSwitchToRegister={() => {
                    setNotification(null);
                    setActiveTab('register');
                  }}
                  isLoading={isLoading}
                />
              )}
            </div>
          </div>
        )}
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
