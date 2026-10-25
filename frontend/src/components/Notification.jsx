import React from 'react';

/**
 * Компонент системных уведомлений (Toast)
 */
export default function Notification({ notification, onClose }) {
  if (!notification) return null;

  const { type = 'info', title, message, details } = notification;

  const icons = {
    success: (
      <svg className="toast-icon success" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
    error: (
      <svg className="toast-icon error" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
    ),
    info: (
      <svg className="toast-icon info" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  };

  return (
    <div className={`toast-container toast-${type}`} role="alert" aria-live="assertive" id="notification-toast">
      <div className="toast-header">
        <div className="toast-title-row">
          {icons[type] || icons.info}
          <strong className="toast-title">{title || (type === 'error' ? 'Ошибка' : 'Успешно')}</strong>
        </div>
        <button
          type="button"
          className="toast-close-btn"
          onClick={onClose}
          aria-label="Закрыть уведомление"
          id="toast-close-btn"
        >
          &times;
        </button>
      </div>
      <div className="toast-body">
        <p className="toast-message">{message}</p>
        {details && typeof details === 'object' && Object.keys(details).length > 0 && (
          <ul className="toast-details-list">
            {Object.entries(details).map(([field, err]) => (
              <li key={field}>
                <span className="toast-field-name">{field}:</span> {String(err)}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
