import React, { useState } from 'react';

/**
 * Форма регистрации пользователя с валидацией в реальном времени
 */
export default function RegisterForm({ onRegister, onSwitchToLogin, isLoading }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [touched, setTouched] = useState({ username: false, password: false, confirmPassword: false });

  // Клиентская валидация
  const errors = {};
  if (!username.trim()) {
    errors.username = 'Имя пользователя обязательно для заполнения';
  } else if (username.trim().length < 3) {
    errors.username = 'Имя пользователя должно содержать не менее 3 символов';
  } else if (username.trim().length > 50) {
    errors.username = 'Имя пользователя не должно превышать 50 символов';
  }

  if (!password) {
    errors.password = 'Пароль обязателен для заполнения';
  } else if (password.length < 6) {
    errors.password = 'Пароль должен содержать как минимум 6 символов';
  }

  if (password && confirmPassword && password !== confirmPassword) {
    errors.confirmPassword = 'Введенные пароли не совпадают';
  }

  const isFormValid = Object.keys(errors).length === 0 && confirmPassword.length > 0;

  // Расчет надежности пароля
  const calculatePasswordStrength = (pass) => {
    if (!pass) return { score: 0, label: 'Пусто', color: 'gray' };
    let score = 0;
    if (pass.length >= 6) score += 1;
    if (pass.length >= 10) score += 1;
    if (/[A-Z]/.test(pass)) score += 1;
    if (/[0-9]/.test(pass)) score += 1;
    if (/[^A-Za-z0-9]/.test(pass)) score += 1;

    if (score <= 2) return { score: 1, label: 'Слабый', color: '#ef4444' };
    if (score <= 3) return { score: 2, label: 'Средний', color: '#f59e0b' };
    return { score: 3, label: 'Надежный', color: '#10b981' };
  };

  const strength = calculatePasswordStrength(password);

  const handleSubmit = (e) => {
    e.preventDefault();
    setTouched({ username: true, password: true, confirmPassword: true });

    if (!isFormValid) {
      return;
    }

    onRegister({ username: username.trim(), password });
  };

  return (
    <div className="auth-card" id="register-card">
      <div className="card-header">
        <h1 className="card-title">Создать аккаунт</h1>
        <p className="card-subtitle">
          Зарегистрируйтесь для доступа к защищенным ресурсам платформы GHInternship
        </p>
      </div>

      <form onSubmit={handleSubmit} noValidate className="auth-form" id="register-form">
        {/* Поле Имя пользователя */}
        <div className="form-group">
          <label htmlFor="register-username" className="form-label">
            Имя пользователя <span className="required-star">*</span>
          </label>
          <div className="input-wrapper">
            <svg className="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            <input
              id="register-username"
              type="text"
              autoComplete="username"
              className={`form-input ${touched.username && errors.username ? 'input-error' : ''}`}
              placeholder="Введите логин (от 3 до 50 символов)"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onBlur={() => setTouched((prev) => ({ ...prev, username: true }))}
              disabled={isLoading}
              required
            />
          </div>
          {touched.username && errors.username && (
            <span className="field-error-text" id="register-username-error">
              {errors.username}
            </span>
          )}
        </div>

        {/* Поле Пароль */}
        <div className="form-group">
          <label htmlFor="register-password" className="form-label">
            Пароль <span className="required-star">*</span>
          </label>
          <div className="input-wrapper">
            <svg className="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            <input
              id="register-password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="new-password"
              className={`form-input ${touched.password && errors.password ? 'input-error' : ''}`}
              placeholder="Минимум 6 символов"
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
            <span className="field-error-text" id="register-password-error">
              {errors.password}
            </span>
          )}

          {/* Индикатор надежности пароля */}
          {password.length > 0 && (
            <div className="strength-meter">
              <div className="strength-bars">
                <div
                  className="strength-bar"
                  style={{
                    backgroundColor: strength.score >= 1 ? strength.color : '#334155',
                  }}
                />
                <div
                  className="strength-bar"
                  style={{
                    backgroundColor: strength.score >= 2 ? strength.color : '#334155',
                  }}
                />
                <div
                  className="strength-bar"
                  style={{
                    backgroundColor: strength.score >= 3 ? strength.color : '#334155',
                  }}
                />
              </div>
              <span className="strength-label" style={{ color: strength.color }}>
                Надежность: {strength.label}
              </span>
            </div>
          )}
        </div>

        {/* Подтверждение пароля */}
        <div className="form-group">
          <label htmlFor="register-confirm-password" className="form-label">
            Повторите пароль <span className="required-star">*</span>
          </label>
          <div className="input-wrapper">
            <svg className="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
            <input
              id="register-confirm-password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="new-password"
              className={`form-input ${touched.confirmPassword && errors.confirmPassword ? 'input-error' : ''}`}
              placeholder="Повторите введенный пароль"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              onBlur={() => setTouched((prev) => ({ ...prev, confirmPassword: true }))}
              disabled={isLoading}
              required
            />
          </div>
          {touched.confirmPassword && errors.confirmPassword && (
            <span className="field-error-text" id="register-confirm-password-error">
              {errors.confirmPassword}
            </span>
          )}
        </div>

        {/* Кнопка отправки формы */}
        <button
          type="submit"
          className="btn btn-primary btn-block submit-btn"
          disabled={isLoading}
          id="register-submit-btn"
        >
          {isLoading ? (
            <span className="loading-spinner-wrap">
              <span className="spinner"></span>
              Регистрация...
            </span>
          ) : (
            'Создать учетную запись'
          )}
        </button>
      </form>

      <div className="card-footer">
        <p className="switch-prompt">
          Уже зарегистрированы?{' '}
          <button
            type="button"
            className="link-btn"
            onClick={onSwitchToLogin}
            id="switch-to-login-btn"
          >
            Войти в систему
          </button>
        </p>
      </div>
    </div>
  );
}
