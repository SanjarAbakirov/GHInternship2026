import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { getUserChatSessions } from '../services/api';

/**
 * Вспомогательное форматирование даты последнего обновления сессии
 * @param {string} dateString - ISO строка даты/времени
 * @returns {string} ЧЧ:ММ для сегодняшних сессий или ДД.ММ для более ранних
 */
function formatSessionTime(dateString) {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '';
    const now = new Date();
    const isToday =
      date.getDate() === now.getDate() &&
      date.getMonth() === now.getMonth() &&
      date.getFullYear() === now.getFullYear();

    if (isToday) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
    return date.toLocaleDateString([], { day: '2-digit', month: '2-digit' });
  } catch {
    return '';
  }
}

/**
 * Компонент боковой панели (Sidebar) истории диалогов пользователя.
 * При загрузке выполняет запрос к `GET /api/chat/sessions`, отображает список
 * тем диалогов в виде кликабельных ссылок, поддерживает состояния загрузки (loading),
 * ошибок (error), пустого списка (empty state) и фильтрации по ключевым словам.
 *
 * @param {Object} props
 * @param {number|null} [props.activeSessionId] - Идентификатор текущей активной сессии
 * @param {Function} [props.onSelectSession] - Обработчик клика по сессии: (sessionId) => void
 * @param {Function} [props.onNewChat] - Обработчик начала нового диалога
 * @param {number} [props.refreshTrigger] - Ключ/триггер принудительного обновления списка
 * @param {boolean} [props.isOpen] - Флаг видимости панели (для мобильных/сворачиваемых экранов)
 * @param {Function} [props.onToggle] - Переключатель видимости панели
 * @param {string} [props.className] - Дополнительный CSS класс
 */
export default function ChatSidebar({
  activeSessionId = null,
  onSelectSession,
  onNewChat,
  refreshTrigger = 0,
  isOpen = true,
  onToggle,
  className = '',
}) {
  const { token } = useAuth();
  const [sessions, setSessions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const handleRefresh = useCallback(() => {
    setReloadKey((prev) => prev + 1);
  }, []);

  // Загрузка сессий при монтировании компонента и при изменении токена, refreshTrigger или reloadKey
  useEffect(() => {
    let isCancelled = false;

    const fetchSessions = async () => {
      setIsLoading(true);
      setError(null);
      console.log('[ChatSidebar] Вызов GET /api/chat/sessions для загрузки истории диалогов...');

      try {
        const data = await getUserChatSessions(token);
        if (!isCancelled) {
          console.log(`[ChatSidebar] Успешно получено сессий: ${data?.length || 0}`);
          setSessions(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        if (!isCancelled) {
          console.error('[ChatSidebar] Ошибка при загрузке истории диалогов:', err);
          let userMessage = err.message || 'Не удалось загрузить историю диалогов';
          if (err.status === 401) {
            userMessage = 'Сессия истекла. Пожалуйста, выполните вход повторно.';
          } else if (err.status >= 500) {
            userMessage = 'Сервер временно недоступен. Попробуйте обновить позже.';
          }
          setError(userMessage);
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    };

    fetchSessions();

    return () => {
      isCancelled = true;
    };
  }, [token, refreshTrigger, reloadKey]);

  // Локальная фильтрация сессий по поисковому запросу
  const filteredSessions = sessions.filter((session) => {
    if (!searchQuery.trim()) return true;
    const query = searchQuery.toLowerCase();
    const matchTitle = session.title?.toLowerCase().includes(query);
    const matchPreview = session.lastMessagePreview?.toLowerCase().includes(query);
    return matchTitle || matchPreview;
  });

  return (
    <aside
      className={`chat-history-sidebar ${isOpen ? 'sidebar-visible' : 'sidebar-collapsed'} ${className}`}
      aria-label="История диалогов с AI"
      data-testid="chat-history-sidebar"
    >
      {/* 1. Шапка панели истории */}
      <div className="sidebar-header-card">
        <div className="sidebar-header-top">
          <div className="sidebar-title-badge-group">
            <h3 className="sidebar-title">История диалогов</h3>
            <span
              className="sidebar-count-badge"
              title="Количество сохраненных диалогов"
              aria-label={`Сохранено диалогов: ${sessions.length}`}
            >
              {sessions.length}
            </span>
          </div>

          {onToggle && (
            <button
              type="button"
              className="sidebar-toggle-btn"
              onClick={onToggle}
              title="Свернуть историю диалогов"
              aria-label="Свернуть панель истории"
            >
              ✕
            </button>
          )}
        </div>

        {/* Кнопка «+ Новый диалог» */}
        <button
          type="button"
          className="btn-new-chat-sidebar"
          onClick={() => {
            console.log('[ChatSidebar] Клик: начало нового диалога');
            onNewChat?.();
          }}
          title="Создать новую беседу с AI"
          aria-label="Начать новый диалог"
          data-testid="new-chat-button"
        >
          <span className="new-chat-plus" aria-hidden="true">+</span>
          <span className="new-chat-text">Новый диалог</span>
        </button>

        {/* Поле поиска по темам бесед (отображается при наличии сессий) */}
        {sessions.length > 0 && (
          <div className="sidebar-search-container">
            <input
              type="text"
              className="sidebar-search-field"
              placeholder="Поиск по темам..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              aria-label="Поиск по сохраненным диалогам"
            />
            {searchQuery && (
              <button
                type="button"
                className="sidebar-search-clear"
                onClick={() => setSearchQuery('')}
                title="Очистить поиск"
                aria-label="Очистить поиск"
              >
                ✕
              </button>
            )}
          </div>
        )}
      </div>

      {/* 2. Тело сайдбара: индикатор загрузки / ошибка / пустой список / список кликабельных ссылок */}
      <div className="sidebar-content-scroll" aria-busy={isLoading}>
        {/* Состояние загрузки (Loading State) */}
        {isLoading && (
          <div className="sidebar-feedback-state sidebar-loading-state" data-testid="sidebar-loading">
            <div className="sidebar-spinner" aria-hidden="true"></div>
            <p className="sidebar-feedback-text">Загрузка истории диалогов...</p>
          </div>
        )}

        {/* Состояние ошибки (Error State) */}
        {!isLoading && error && (
          <div className="sidebar-feedback-state sidebar-error-state" role="alert" data-testid="sidebar-error">
            <div className="sidebar-state-icon" aria-hidden="true">⚠️</div>
            <p className="sidebar-error-title">Не удалось загрузить историю</p>
            <p className="sidebar-feedback-text">{error}</p>
            <button
              type="button"
              className="btn btn-sm btn-retry-sidebar"
              onClick={handleRefresh}
              aria-label="Повторить загрузку истории диалогов"
            >
              🔄 Повторить
            </button>
          </div>
        )}

        {/* Пустое состояние (Empty State) — 0 сессий */}
        {!isLoading && !error && sessions.length === 0 && (
          <div className="sidebar-feedback-state sidebar-empty-state" data-testid="sidebar-empty">
            <div className="sidebar-state-icon" aria-hidden="true">💬</div>
            <p className="sidebar-empty-title">Нет сохраненных диалогов</p>
            <p className="sidebar-feedback-text">
              Задайте вопрос AI-ассистенту, и беседа автоматически сохранится здесь.
            </p>
          </div>
        )}

        {/* Ничего не найдено по фильтру */}
        {!isLoading && !error && sessions.length > 0 && filteredSessions.length === 0 && (
          <div className="sidebar-feedback-state sidebar-no-results">
            <p className="sidebar-feedback-text">По запросу «{searchQuery}» диалогов не найдено</p>
          </div>
        )}

        {/* 3. Список сохраненных диалогов в виде кликабельных ссылок */}
        {!isLoading && !error && filteredSessions.length > 0 && (
          <nav className="sidebar-sessions-nav" aria-label="Сохраненные диалоги пользователя">
            <ul className="sidebar-sessions-list" role="list">
              {filteredSessions.map((session) => {
                const isActive = Number(session.id) === Number(activeSessionId);
                const displayTime = formatSessionTime(session.updatedAt || session.createdAt);

                return (
                  <li key={session.id} className="sidebar-session-item">
                    <a
                      href={`#session-${session.id}`}
                      className={`session-nav-link ${isActive ? 'session-link-active' : ''}`}
                      onClick={(e) => {
                        e.preventDefault();
                        console.log(`[ChatSidebar] Выбрана сессия [id=${session.id}, title='${session.title}']`);
                        onSelectSession?.(session.id);
                      }}
                      title={session.title || 'Диалог без названия'}
                      aria-current={isActive ? 'true' : undefined}
                      data-session-id={session.id}
                      role="link"
                    >
                      <div className="session-link-header">
                        <span className="session-link-title">
                          {session.title || 'Новый диалог'}
                        </span>
                        {displayTime && (
                          <time className="session-link-time" dateTime={session.updatedAt || session.createdAt}>
                            {displayTime}
                          </time>
                        )}
                      </div>

                      <div className="session-link-details">
                        <span className="session-link-count" title="Количество сообщений">
                          💬 {session.messageCount || 0}
                        </span>
                        {session.lastMessagePreview && (
                          <span className="session-link-preview" title={session.lastMessagePreview}>
                            {session.lastMessagePreview}
                          </span>
                        )}
                      </div>
                    </a>
                  </li>
                );
              })}
            </ul>
          </nav>
        )}
      </div>

      {/* 4. Подвал панели истории с кнопкой ручного обновления */}
      <div className="sidebar-footer-card">
        <button
          type="button"
          className="btn-refresh-history"
          onClick={handleRefresh}
          disabled={isLoading}
          title="Обновить список сессий диалогов"
          aria-label="Обновить список сессий"
          data-testid="refresh-history-btn"
        >
          <span className="refresh-history-icon" aria-hidden="true">🔄</span>
          <span>Обновить список</span>
        </button>
      </div>
    </aside>
  );
}
