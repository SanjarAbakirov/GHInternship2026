import React, { useState } from 'react';

/**
 * Форма входа (аутентификации) пользователя
 */
export default function LoginForm({ onLogin, onSwitchToRegister, isLoading }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [touched, setTouched] = useState({ username: false, password: false });

  // Валидация полей
  const errors = {};
  if (!username.trim()) {
    errors.username = 'Укажите имя пользователя';
  }
  if (!password) {
    errors.password = 'Укажите пароль';
  }

  const isFormValid = Object.keys(errors).length === 0;

  const handleSubmit = (e) => {
    e.preventDefault();
    setTouched({ username: true, password: true });

    if (!isFormValid) {
      return;
    }

    onLogin({ username: username.trim(), password, rememberMe });
  };

  return (
    <div className="auth-card" id="login-card">
      <div className="card-header">
        <h1 className="card-title">Вход в систему</h1>
        <p className="card-subtitle">
          Введите ваши учетные данные для авторизации в системе
        </p>
      </div>

      <form onSubmit={handleSubmit} noValidate className="auth-form" id="login-form">
        {/* Поле Имя пользователя */}
        <div className="form-group">
          <label htmlFor="login-username" className="form-label">
            Имя пользователя <span className="required-star">*</span>
          </label>
          <div className="input-wrapper">
            <svg className="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            <input
              id="login-username"
              type="text"
              autoComplete="username"
              className={`form-input ${touched.username && errors.username ? 'input-error' : ''}`}
              placeholder="Ваш логин"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onBlur={() => setTouched((prev) => ({ ...prev, username: true }))}
              disabled={isLoading}
              required
            />
          </div>
          {touched.username && errors.username && (
            <span className="field-error-text" id="login-username-error">
              {errors.username}
            </span>
          )}
        </div>

        {/* Поле Пароль */}
        <div className="form-group">
          <div className="label-row">
            <label htmlFor="login-password" className="form-label">
              Пароль <span className="required-star">*</span>
            </label>
          </div>
          <div className="input-wrapper">
            <svg className="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            <input
              id="login-password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="current-password"
              className={`form-input ${touched.password && errors.password ? 'input-error' : ''}`}
              placeholder="Ваш пароль"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onBlur={() => setTouched((prev) => ({ ...prev, password: true }))}
              disabled={isLoading}
              required
            />
            <button
              type="button"
              className="toggle-password-btn"
              onClick={() => setShowPassword(!showPassword)}
              aria-label={showPassword ? 'Скрыть пароль' : 'Показать пароль'}
            >
              {showPassword ? 'Скрыть' : 'Показать'}
            </button>
          </div>
          {touched.password && errors.password && (
            <span className="field-error-text" id="login-password-error">
              {errors.password}
            </span>
          )}
        </div>

        {/* Запомнить меня */}
        <div className="form-options">
          <label className="checkbox-label" htmlFor="remember-me">
            <input
              id="remember-me"
              type="checkbox"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
              disabled={isLoading}
            />
            <span>Запомнить меня на этом устройстве</span>
          </label>
        </div>

        {/* Кнопка отправки */}
        <button
          type="submit"
          className="btn btn-primary btn-block submit-btn"
          disabled={isLoading}
          id="login-submit-btn"
        >
          {isLoading ? (
            <span className="loading-spinner-wrap">
              <span className="spinner"></span>
              Вход в систему...
            </span>
          ) : (
            'Войти'
          )}
        </button>
      </form>

      <div className="card-footer">
        <p className="switch-prompt">
          Еще нет аккаунта?{' '}
          <button
            type="button"
            className="link-btn"
            onClick={onSwitchToRegister}
            id="switch-to-register-btn"
          >
            Зарегистрироваться
          </button>
        </p>
      </div>
    </div>
  );
}
