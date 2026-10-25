import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import RegisterForm from './components/RegisterForm';
import LoginForm from './components/LoginForm';
import Dashboard from './components/Dashboard';
import Notification from './components/Notification';
import { registerUser, loginUser, checkBackendHealth } from './services/api';
import './App.css';

export default function App() {
  const [activeTab, setActiveTab] = useState('login'); // 'login' | 'register'
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const saved = localStorage.getItem('gh_internship_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [isLoading, setIsLoading] = useState(false);
  const [notification, setNotification] = useState(null);
  const [backendStatus, setBackendStatus] = useState({ online: false, message: 'Проверка...' });

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
      setNotification({
        type: 'success',
        title: 'Регистрация успешна!',
        message: `Пользователь "${result.username || username}" успешно создан. Теперь вы можете войти в систему.`,
      });
      setActiveTab('login');
    } catch (err) {
      console.error('[App] Ошибка при регистрации:', err);
      setNotification({
        type: 'error',
        title: 'Ошибка регистрации',
        message: err.message,
        details: err.details,
      });
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
    } catch (err) {
      console.error('[App] Ошибка при входе:', err);
      setNotification({
        type: 'error',
        title: 'Ошибка авторизации',
        message: err.message || 'Неверное имя пользователя или пароль',
        details: err.details,
      });
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
              {activeTab === 'register' ? (
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
