import React, { useState } from 'react';

/**
 * Личный кабинет (Dashboard) успешно аутентифицированного пользователя
 */
export default function Dashboard({ user, onLogout, backendStatus, onRefreshBackend }) {
  const [helloResponse, setHelloResponse] = useState(null);
  const [isPinging, setIsPinging] = useState(false);

  const handleTestBackend = async () => {
    setIsPinging(true);
    try {
      const res = await fetch('/hello');
      const text = await res.text();
      setHelloResponse({ success: true, text, timestamp: new Date().toLocaleTimeString() });
    } catch (err) {
      setHelloResponse({ success: false, text: err.message, timestamp: new Date().toLocaleTimeString() });
    } finally {
      setIsPinging(false);
    }
  };

  return (
    <div className="dashboard-container" id="dashboard-view">
      {/* Приветственный блок */}
      <div className="dashboard-hero">
        <div className="hero-badge">Сессия активна</div>
        <h1 className="dashboard-title">
          Добро пожаловать, <span className="gradient-text">{user.username}</span>!
        </h1>
        <p className="dashboard-subtitle">
          Вы успешно прошли аутентификацию в системе GHInternship 2026. Ваши учетные данные проверены и защищены хэшированием BCrypt.
        </p>
      </div>

      {/* Информационные карточки */}
      <div className="dashboard-grid">
        {/* Карточка пользователя */}
        <div className="dashboard-card user-info-card">
          <div className="card-top">
            <div className="user-large-avatar">
              {user.username.charAt(0).toUpperCase()}
            </div>
            <div className="user-details">
              <h2 className="user-heading">{user.username}</h2>
              <span className="user-role-badge">Пользователь платформы</span>
            </div>
          </div>

          <div className="info-rows">
            <div className="info-row">
              <span className="info-label">Идентификатор (ID):</span>
              <span className="info-value font-mono">{user.id || 'Авторизован'}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Статус аккаунта:</span>
              <span className="status-tag status-active">Активен (Active)</span>
            </div>
            <div className="info-row">
              <span className="info-label">Алгоритм пароля:</span>
              <span className="info-value">Spring Security BCrypt (10 раундов)</span>
            </div>
            <div className="info-row">
              <span className="info-label">Время авторизации:</span>
              <span className="info-value">{user.loginTime || 'Только что'}</span>
            </div>
          </div>

          <div className="card-actions">
            <button
              type="button"
              className="btn btn-outline btn-block"
              onClick={onLogout}
              id="dashboard-logout-btn"
            >
              Завершить сеанс (Выйти)
            </button>
          </div>
        </div>

        {/* Карточка взаимодействия с бэкендом */}
        <div className="dashboard-card backend-info-card">
          <div className="card-top-between">
            <h2 className="card-section-title">Интеграция со Spring Boot</h2>
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={onRefreshBackend}
              title="Перепроверить связь"
            >
              {backendStatus?.online ? '🟢 Онлайн' : '🔴 Офлайн'}
            </button>
          </div>
          <p className="card-desc">
            Проверка взаимодействия фронтенда ReactJS и контроллеров бэкенда через REST API.
          </p>

          <div className="backend-endpoints-list">
            <div className="endpoint-item">
              <span className="http-badge post">POST</span>
              <span className="endpoint-path">/api/auth/register</span>
              <span className="endpoint-desc">Регистрация (BCrypt hash)</span>
            </div>
            <div className="endpoint-item">
              <span className="http-badge post">POST</span>
              <span className="endpoint-path">/api/auth/login</span>
              <span className="endpoint-desc">Аутентификация пароля</span>
            </div>
            <div className="endpoint-item">
              <span className="http-badge get">GET</span>
              <span className="endpoint-path">/hello</span>
              <span className="endpoint-desc">Публичный эндпоинт проверки</span>
            </div>
          </div>

          <div className="test-api-box">
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={handleTestBackend}
              disabled={isPinging}
              id="test-hello-api-btn"
            >
              {isPinging ? 'Отправка запроса...' : 'Тестировать GET /hello'}
            </button>

            {helloResponse && (
              <div className={`api-response-pill ${helloResponse.success ? 'pill-success' : 'pill-error'}`}>
                <span className="pill-time">[{helloResponse.timestamp}]</span>
                <span className="pill-text">{helloResponse.text}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Технологический стек проекта */}
      <div className="tech-stack-section">
        <h3 className="tech-stack-title">Архитектура и стек технологий</h3>
        <div className="tech-badges">
          <span className="tech-tag">Spring Boot 4.0.0</span>
          <span className="tech-tag">Spring Security 7</span>
          <span className="tech-tag">Spring Data JPA</span>
          <span className="tech-tag">H2 In-Memory DB</span>
          <span className="tech-tag">React 19</span>
          <span className="tech-tag">Vite 8</span>
          <span className="tech-tag">Vanilla CSS Glassmorphism</span>
        </div>
      </div>
    </div>
  );
}
