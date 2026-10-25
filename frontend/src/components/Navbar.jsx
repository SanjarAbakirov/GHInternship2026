import React from 'react';

/**
 * Навигационная шапка с индикатором статуса бэкенда Spring Boot
 */
export default function Navbar({ activeTab, setActiveTab, backendStatus, onRefreshBackend, currentUser, onLogout }) {
  return (
    <header className="navbar-container">
      <div className="navbar-brand">
        <div className="brand-logo-glow">
          <svg className="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
          </svg>
        </div>
        <div className="brand-text">
          <span className="brand-title">GHInternship</span>
          <span className="brand-subtitle">Spring Boot & React</span>
        </div>
      </div>

      <div className="navbar-center">
        <div
          className={`status-pill ${backendStatus.online ? 'status-online' : 'status-offline'}`}
          title={backendStatus.message || 'Проверка соединения с бэкендом'}
          onClick={onRefreshBackend}
          role="status"
          id="backend-status-pill"
        >
          <span className="status-dot"></span>
          <span className="status-label">
            Бэкенд: {backendStatus.online ? 'На связи (8080)' : 'Недоступен'}
          </span>
          <button
            type="button"
            className="status-refresh-btn"
            title="Обновить статус соединения"
            aria-label="Обновить статус"
            id="refresh-status-btn"
          >
            ↻
          </button>
        </div>
      </div>

      <nav className="navbar-actions" aria-label="Основная навигация">
        {currentUser ? (
          <div className="user-profile-menu">
            <span className="user-badge" id="navbar-user-badge">
              <span className="user-avatar-initial">{currentUser.username?.charAt(0).toUpperCase()}</span>
              <span className="user-name">{currentUser.username}</span>
            </span>
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={onLogout}
              id="logout-btn-nav"
            >
              Выйти
            </button>
          </div>
        ) : (
          <div className="nav-tabs" role="tablist">
            <button
              type="button"
              role="tab"
              aria-selected={activeTab === 'login'}
              className={`nav-tab-btn ${activeTab === 'login' ? 'active' : ''}`}
              onClick={() => setActiveTab('login')}
              id="tab-login-btn"
            >
              Вход
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={activeTab === 'register'}
              className={`nav-tab-btn ${activeTab === 'register' ? 'active' : ''}`}
              onClick={() => setActiveTab('register')}
              id="tab-register-btn"
            >
              Регистрация
            </button>
          </div>
        )}
      </nav>
    </header>
  );
}
