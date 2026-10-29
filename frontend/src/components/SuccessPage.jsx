import React, { useState, useEffect } from 'react';

/**
 * Страница успешной регистрации (Success Page)
 * Отображает результат успешного создания аккаунта, flash-сообщение от бэкенда,
 * информацию о безопасности учетной записи и предоставляет таймер автоматического перехода на форму входа.
 */
export default function SuccessPage({ username, message, onSwitchToLogin, onGoRegisterAnother }) {
  const [countdown, setCountdown] = useState(6);
  const [isPaused, setIsPaused] = useState(false);

  useEffect(() => {
    if (isPaused) return;

    if (countdown <= 0) {
      onSwitchToLogin();
      return;
    }

    const timer = setInterval(() => {
      setCountdown((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [countdown, isPaused, onSwitchToLogin]);

  const effectiveUser = username || 'Пользователь';
  const effectiveMessage = message || `Регистрация прошла успешно! Добро пожаловать, ${effectiveUser}!`;

  return (
    <div className="auth-card success-card" role="region" aria-labelledby="success-heading">
      {/* Анимированная светящаяся иконка успеха */}
      <div className="success-icon-wrapper" aria-hidden="true">
        <div className="success-icon-pulse"></div>
        <svg className="success-check-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
          <polyline points="20 6 9 17 4 12" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </div>

      <div className="auth-header text-center">
        <div className="success-badge">
          <span className="badge-dot"></span>
          <span>HTTP 201 Created &bull; Регистрация подтверждена</span>
        </div>
        <h2 id="success-heading" className="auth-title success-title">
          Поздравляем с успешной регистрацией!
        </h2>
        <div className="registered-user-pill" title="Зарегистрированный логин">
          <span className="user-icon-small">👤</span>
          <span className="user-login-name">{effectiveUser}</span>
        </div>
        <p className="auth-subtitle success-subtitle" role="status" aria-live="polite">
          {effectiveMessage}
        </p>
      </div>

      {/* Информационный блок о безопасности */}
      <div className="success-info-box">
        <h3 className="info-box-title">Статус аккаунта и безопасность</h3>
        <ul className="success-feature-list">
          <li className="feature-item">
            <span className="feature-icon check-green">✓</span>
            <div className="feature-text">
              <strong>Учетная запись активирована</strong>
              <span>Данные успешно записаны в базу данных H2</span>
            </div>
          </li>
          <li className="feature-item">
            <span className="feature-icon check-green">✓</span>
            <div className="feature-text">
              <strong>Хэширование BCrypt с солью</strong>
              <span>Пароль защищен стойким односторонним шифрованием</span>
            </div>
          </li>
          <li className="feature-item">
            <span className="feature-icon check-green">✓</span>
            <div className="feature-text">
              <strong>Готовность к авторизации</strong>
              <span>Вы можете сразу войти, используя указанный логин и пароль</span>
            </div>
          </li>
        </ul>
      </div>

      {/* Блок автоматического перенаправления с таймером */}
      <div className="redirect-timer-box">
        <div className="timer-icon">⏳</div>
        <div className="timer-content">
          <span className="timer-text">
            {isPaused ? (
              'Автоматический переход приостановлен'
            ) : (
              <>
                Автоматический переход ко входу через{' '}
                <strong className="timer-seconds">{countdown}</strong> сек...
              </>
            )}
          </span>
          <button
            type="button"
            className="timer-pause-btn"
            onClick={() => setIsPaused(!isPaused)}
            title={isPaused ? 'Возобновить обратный отсчет' : 'Приостановить обратный отсчет'}
          >
            {isPaused ? 'Возобновить' : 'Остановить'}
          </button>
        </div>
      </div>

      {/* Кнопки действий */}
      <div className="success-actions">
        <button
          type="button"
          className="btn btn-primary btn-block success-login-btn"
          onClick={onSwitchToLogin}
          id="success-login-now-btn"
        >
          <span>Войти в аккаунт прямо сейчас</span>
          <span className="btn-arrow">→</span>
        </button>

        {onGoRegisterAnother && (
          <button
            type="button"
            className="btn btn-ghost btn-block mt-2"
            onClick={onGoRegisterAnother}
            id="register-another-btn"
          >
            Зарегистрировать другого пользователя
          </button>
        )}
      </div>
    </div>
  );
}
