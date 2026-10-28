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
  const [serverErrors, setServerErrors] = useState({});
  const [serverGeneralError, setServerGeneralError] = useState('');

  // Валидация полей
  const errors = {};
  if (!username.trim()) {
    errors.username = 'Укажите имя пользователя';
  }
  if (!password) {
    errors.password = 'Укажите пароль';
  }

  const isFormValid = Object.keys(errors).length === 0;

  // Итоговые ошибки (клиентские или ошибки с бэкенда)
  const usernameError = (touched.username && errors.username) || serverErrors.username;
  const passwordError = (touched.password && errors.password) || serverErrors.password;

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log('[LoginForm] Сабмит формы входа на POST /api/auth/login');
    setTouched({ username: true, password: true });
    setServerErrors({});
    setServerGeneralError('');

    if (!isFormValid) {
      console.warn('[LoginForm] Клиентская валидация не пройдена:', errors);
      return;
    }

    console.log(`[LoginForm] Отправка данных авторизации для пользователя: "${username.trim()}"`);
    if (onLogin) {
      const result = await onLogin({ username: username.trim(), password, rememberMe });
      if (result && !result.success) {
        console.warn('[LoginForm] Получена ошибка от бэкенда:', result);
        if (result.validationErrors && Object.keys(result.validationErrors).length > 0) {
          setServerErrors(result.validationErrors);
          setServerGeneralError(result.message || 'Ошибка валидации данных на сервере (400)');
        } else if (result.status === 401) {
          setServerGeneralError(result.message || 'Неверное имя пользователя или пароль (401)');
        } else if (result.message) {
          setServerGeneralError(result.message);
        }
      }
    }
  };

  return (
    <div className="auth-card" id="login-card">
      <div className="card-header">
        <h1 className="card-title">Вход в систему</h1>
        <p className="card-subtitle">
          Введите ваши учетные данные для авторизации в системе
        </p>
      </div>

      {/* Баннер ошибки бэкенда */}
      {serverGeneralError && (
        <div className="server-alert server-alert-error" role="alert" id="login-server-error">
          <svg className="server-alert-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <div className="server-alert-content">
            <div className="server-alert-title">Ошибка авторизации</div>
            <p className="server-alert-message">{serverGeneralError}</p>
          </div>
        </div>
      )}

      <form
        action="/api/auth/login"
        method="POST"
        onSubmit={handleSubmit}
        noValidate
        className="auth-form"
        id="login-form"
      >
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
              name="username"
              type="text"
              autoComplete="username"
              spellCheck="false"
              autoCapitalize="none"
              className={`form-input ${usernameError ? 'input-error' : ''}`}
              placeholder="Ваш логин"
              value={username}
              onChange={(e) => {
                setUsername(e.target.value);
                if (serverErrors.username) {
                  setServerErrors((prev) => ({ ...prev, username: null }));
                }
              }}
              onBlur={() => setTouched((prev) => ({ ...prev, username: true }))}
              disabled={isLoading}
              aria-invalid={usernameError ? 'true' : 'false'}
              aria-describedby={usernameError ? 'login-username-error' : undefined}
              required
            />
          </div>
          {usernameError && (
            <span className="field-error-text" id="login-username-error" role="alert">
              <svg className="error-small-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              {usernameError}
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
              name="password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="current-password"
              className={`form-input ${passwordError ? 'input-error' : ''}`}
              placeholder="Ваш пароль"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (serverErrors.password) {
                  setServerErrors((prev) => ({ ...prev, password: null }));
                }
              }}
              onBlur={() => setTouched((prev) => ({ ...prev, password: true }))}
              disabled={isLoading}
              aria-invalid={passwordError ? 'true' : 'false'}
              aria-describedby={passwordError ? 'login-password-error' : undefined}
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
          {passwordError && (
            <span className="field-error-text" id="login-password-error" role="alert">
              <svg className="error-small-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              {passwordError}
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
