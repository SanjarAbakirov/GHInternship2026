import React, { useState, useRef, useEffect, useCallback, memo } from 'react';
import { useAuth } from '../context/AuthContext';
import { sendChatMessage } from '../services/api';

let fallbackCounter = 0;

/**
 * Генератор гарантированно уникальных идентификаторов для React `key`.
 * Предотвращает коллизии ключей при мгновенном добавлении нескольких сообщений подряд.
 *
 * @param {string} prefix - Префикс идентификатора (user, ai, err, welcome)
 * @returns {string} Уникальный ключ
 */
function generateMessageId(prefix = 'msg') {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return `${prefix}-${crypto.randomUUID()}`;
  }
  fallbackCounter += 1;
  return `${prefix}-${Date.now()}-${fallbackCounter}-${Math.random().toString(36).slice(2, 7)}`;
}

/**
 * Форматирование времени сообщения (ЧЧ:ММ)
 */
function formatTimestamp() {
  return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

/**
 * Мемоизированный элемент сообщения для эффективного рендеринга списка в React.
 * Благодаря уникальному `key={msg.id}` и `React.memo` уже отрисованные реплики
 * не перерисовываются при добавлении новых сообщений.
 */
const ChatMessageItem = memo(function ChatMessageItem({ msg, currentUsername }) {
  const bubbleClass =
    msg.sender === 'user'
      ? 'chat-bubble-user'
      : msg.sender === 'system'
      ? 'chat-bubble-system'
      : 'chat-bubble-ai';

  const senderLabel =
    msg.sender === 'user'
      ? currentUsername || 'Вы'
      : msg.sender === 'system'
      ? 'Системное предупреждение'
      : 'AI Ассистент';

  const avatarContent =
    msg.sender === 'user'
      ? currentUsername?.charAt(0).toUpperCase() || 'U'
      : msg.sender === 'system'
      ? '⚠️'
      : '🤖';

  return (
    <article className={`chat-bubble-wrapper ${bubbleClass}`} data-message-id={msg.id}>
      <div className="chat-bubble-avatar" aria-hidden="true">
        {avatarContent}
      </div>
      <div className="chat-bubble-content">
        <div className="chat-bubble-meta">
          <span className="chat-sender-name">{senderLabel}</span>
          <span className="chat-message-time">{msg.timestamp}</span>
        </div>
        <div className="chat-bubble-text">{msg.text}</div>
      </div>
    </article>
  );
});

/**
 * Переиспользуемый компонент AI-чата (Chat Component).
 * Поддерживает динамическое обновление диалога (мгновенное отображение реплик пользователя
 * и ответов AI), уникальные React ключи и автоматическую плавную прокрутку к последнему сообщению.
 *
 * @param {Object} props
 * @param {string} [props.title] - Заголовок чата
 * @param {string} [props.subtitle] - Подзаголовок / описание
 * @param {string} [props.placeholder] - Плейсхолдер поля ввода
 * @param {Array} [props.initialMessages] - Начальный список сообщений
 * @param {Function} [props.onSendMessage] - Кастомный обработчик отправки (message) => Promise
 * @param {Array<string>} [props.quickPrompts] - Массив быстрых подсказок/вопросов
 * @param {boolean} [props.showQuickPrompts] - Отображать ли блок быстрых подсказок
 * @param {boolean} [props.showClearButton] - Отображать ли кнопку очистки истории
 * @param {number} [props.maxMessageLength] - Максимальная длина сообщения
 * @param {string} [props.className] - Дополнительный CSS класс контейнера
 */
export default function Chat({
  title = 'Интеллектуальный AI Ассистент',
  subtitle,
  placeholder = 'Задайте вопрос или напишите сообщение AI-ассистенту...',
  initialMessages,
  onSendMessage,
  quickPrompts = [
    '💡 Что такое Spring Boot и какие у него преимущества?',
    '🔐 Как работает хэширование паролей с помощью BCrypt?',
    '🛡️ Как работает Protected Route в React?',
    '⚡ Расскажи про JWT токены и архитектуру безопасности',
  ],
  showQuickPrompts = true,
  showClearButton = true,
  maxMessageLength = 2000,
  className = '',
}) {
  const { currentUser, token } = useAuth();

  // Локальное состояние сообщений с гарантированной уникальностью id для React key
  const [messages, setMessages] = useState(() => {
    if (initialMessages && Array.isArray(initialMessages) && initialMessages.length > 0) {
      return initialMessages.map((msg) => ({
        ...msg,
        id: msg.id || generateMessageId(msg.sender || 'init'),
      }));
    }
    return [
      {
        id: generateMessageId('welcome'),
        sender: 'assistant',
        text: `Здравствуйте, ${currentUser?.username || 'пользователь'}! Я интеллектуальный AI-ассистент платформы GHInternship. Чем я могу помочь вам сегодня?`,
        timestamp: formatTimestamp(),
      },
    ];
  });

  const [inputMessage, setInputMessage] = useState('');
  const [isSending, setIsSending] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [showScrollButton, setShowScrollButton] = useState(false);

  const messagesContainerRef = useRef(null);
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);
  const shouldAutoScrollRef = useRef(true);

  /**
   * Плавная прокрутка ленты сообщений к самому последнему элементу.
   * Выполняется через requestAnimationFrame после фиксации обновлений в DOM.
   */
  const scrollToBottom = useCallback((behavior = 'smooth') => {
    requestAnimationFrame(() => {
      if (messagesEndRef.current && typeof messagesEndRef.current.scrollIntoView === 'function') {
        messagesEndRef.current.scrollIntoView({ behavior, block: 'end' });
      } else if (messagesContainerRef.current) {
        messagesContainerRef.current.scrollTop = messagesContainerRef.current.scrollHeight;
      }
      shouldAutoScrollRef.current = true;
      setShowScrollButton(false);
    });
  }, []);

  /**
   * Мониторинг позиции скролла пользователя для интеллектуальной прокрутки
   */
  const handleMessagesScroll = useCallback(() => {
    const container = messagesContainerRef.current;
    if (!container) return;
    const distanceFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
    const isNearBottom = distanceFromBottom < 80;
    shouldAutoScrollRef.current = isNearBottom;
    setShowScrollButton(!isNearBottom);
  }, []);

  // Автоматический скролл к последнему сообщению при обновлении диалога или появлении индикатора набора текста
  useEffect(() => {
    if (shouldAutoScrollRef.current) {
      console.log(`[Chat] Автопрокрутка диалога к последнему сообщению (всего сообщений: ${messages.length})`);
      scrollToBottom('smooth');
    }
  }, [messages, isSending, scrollToBottom]);

  // Установка фокуса на поле ввода при монтировании компонента
  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  /**
   * Асинхронная функция, выполняющая POST-запрос к эндпоинту /api/chat бэкенда.
   * Включает токен аутентификации пользователя (JWT/Bearer) в заголовок Authorization
   * через переиспользуемый клиент apiClient / authFetch.
   *
   * @param {string} messageText - Текст сообщения
   * @returns {Promise<string>} Текст ответа от AI-сервиса
   */
  const sendAuthenticatedMessage = async (messageText) => {
    console.log(
      `[Chat] Вызов POST /api/chat [Пользователь: "${currentUser?.username || 'Guest'}", Token: ${
        token ? 'Присутствует' : 'Отсутствует'
      }]`
    );

    if (onSendMessage && typeof onSendMessage === 'function') {
      const customRes = await onSendMessage(messageText);
      return typeof customRes === 'string' ? customRes : customRes?.response || customRes?.text || '';
    }

    const responseDto = await sendChatMessage(messageText, token);
    return responseDto.response || 'Ответ от AI успешно получен.';
  };

  /**
   * Обработка отправки формы пользователем:
   * 1. Мгновенное добавление сообщения пользователя в историю чата.
   * 2. Асинхронный запрос к бэкенду и добавление ответа AI в UI.
   */
  const handleSend = async (e) => {
    if (e) e.preventDefault();
    const trimmed = inputMessage.trim();
    if (!trimmed || isSending) return;

    const userMessageItem = {
      id: generateMessageId('user'),
      sender: 'user',
      text: trimmed,
      timestamp: formatTimestamp(),
    };

    // Гарантируем прокрутку вниз при отправке нового сообщения
    shouldAutoScrollRef.current = true;

    console.log(`[Chat] Немедленное отображение сообщения пользователя в истории (id=${userMessageItem.id})`);
    setMessages((prev) => [...prev, userMessageItem]);
    setInputMessage('');
    setIsSending(true);
    setErrorMessage(null);

    try {
      const responseText = await sendAuthenticatedMessage(trimmed);

      const aiMessageItem = {
        id: generateMessageId('ai'),
        sender: 'assistant',
        text: responseText,
        timestamp: formatTimestamp(),
      };

      console.log(`[Chat] Ответ AI получен и добавлен в диалог (id=${aiMessageItem.id})`);
      setMessages((prev) => [...prev, aiMessageItem]);
    } catch (err) {
      console.error('[Chat] Ошибка при вызове защищенного эндпоинта /api/chat:', err);
      let displayError = err.message || 'Не удалось получить ответ от AI-сервиса. Попробуйте снова.';

      if (err.status === 401) {
        displayError = 'Ошибка авторизации (401 Unauthorized): требуется действующий токен в заголовке Authorization.';
      } else if (err.status === 400) {
        displayError = 'Ошибка валидации (400 Bad Request): отправлено некорректное сообщение.';
      } else if (err.status === 500) {
        displayError = 'Ошибка сервера (500 Internal Server Error): сбой при обращении к внешнему AI-сервису.';
      }

      setErrorMessage(displayError);

      const errorReplyItem = {
        id: generateMessageId('err'),
        sender: 'system',
        text: `⚠️ Ошибка: ${displayError}`,
        timestamp: formatTimestamp(),
      };
      setMessages((prev) => [...prev, errorReplyItem]);
    } finally {
      setIsSending(false);
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  };

  /**
   * Подстановка быстрого промпта в поле ввода
   */
  const handleSelectQuickPrompt = (promptText) => {
    const cleanedText = promptText.replace(/^[^\wА-Яа-яЁё]+\s*/, '');
    setInputMessage(cleanedText);
    setTimeout(() => {
      inputRef.current?.focus();
    }, 50);
  };

  /**
   * Очистка истории сообщений
   */
  const handleClearChat = () => {
    shouldAutoScrollRef.current = true;
    setMessages([
      {
        id: generateMessageId('welcome'),
        sender: 'assistant',
        text: `История диалога очищена. Чем еще я могу вам помочь, ${currentUser?.username || 'пользователь'}?`,
        timestamp: formatTimestamp(),
      },
    ]);
    setErrorMessage(null);
  };

  const effectiveSubtitle = subtitle || (
    currentUser
      ? `Авторизован как: ${currentUser.username} • Защищенный сеанс`
      : 'Безопасное соединение через Spring Boot API'
  );

  return (
    <div className={`chat-window-container ${className}`} id="chat-component">
      {/* 1. Шапка окна чата (Chat Header) */}
      <header className="chat-header-card" aria-label="Шапка чата">
        <div className="chat-header-left">
          <div className="ai-avatar-glow" aria-hidden="true">
            <svg className="ai-sparkle-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 00-2.456 2.456z"
              />
            </svg>
          </div>
          <div className="ai-header-info">
            <div className="ai-title-row">
              <h2 className="ai-header-title">{title}</h2>
              <span className="ai-model-tag">POST /api/chat</span>
            </div>
            <p className="ai-header-sub">
              {currentUser ? (
                <>
                  Авторизован как: <strong className="user-highlight">{currentUser.username}</strong> &bull; Защищенный сеанс
                </>
              ) : (
                effectiveSubtitle
              )}
            </p>
          </div>
        </div>

        {showClearButton && (
          <div className="chat-header-actions">
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={handleClearChat}
              title="Очистить историю диалога"
              id="clear-chat-history-btn"
            >
              Очистить диалог
            </button>
          </div>
        )}
      </header>

      {/* 2. Блок быстрых подсказок (Quick Prompts) */}
      {showQuickPrompts && quickPrompts.length > 0 && (
        <nav className="quick-prompts-container" aria-label="Быстрые вопросы для AI">
          <span className="quick-prompts-label">Быстрые подсказки:</span>
          <div className="quick-prompts-scroll">
            {quickPrompts.map((prompt) => (
              <button
                key={`prompt-${prompt}`}
                type="button"
                className="quick-chip"
                onClick={() => handleSelectQuickPrompt(prompt)}
              >
                {prompt}
              </button>
            ))}
          </div>
        </nav>
      )}

      {/* 3. Область динамического отображения сообщений (Display Area for Messages) */}
      <div className="chat-messages-wrapper">
        <section
          ref={messagesContainerRef}
          onScroll={handleMessagesScroll}
          className="chat-messages-container"
          role="log"
          aria-live="polite"
          aria-label="История сообщений"
          id="chat-messages-display"
        >
          {messages.map((msg) => (
            <ChatMessageItem
              key={msg.id}
              msg={msg}
              currentUsername={currentUser?.username}
            />
          ))}

          {/* Анимация набора текста AI */}
          {isSending && (
            <div className="chat-bubble-wrapper chat-bubble-ai" aria-label="AI формирует ответ">
              <div className="chat-bubble-avatar" aria-hidden="true">🤖</div>
              <div className="chat-bubble-content">
                <div className="chat-bubble-meta">
                  <span className="chat-sender-name">AI Ассистент</span>
                  <span className="chat-message-time">Генерация...</span>
                </div>
                <div className="typing-indicator" aria-label="AI думает">
                  <span className="typing-dot"></span>
                  <span className="typing-dot"></span>
                  <span className="typing-dot"></span>
                </div>
              </div>
            </div>
          )}

          {/* Якорь для автоматической прокрутки к последнему сообщению */}
          <div ref={messagesEndRef} className="chat-scroll-anchor" aria-hidden="true" />
        </section>

        {/* Плавающая кнопка возврата вниз, если пользователь прокрутил историю вверх */}
        {showScrollButton && (
          <button
            type="button"
            className="scroll-to-bottom-btn"
            onClick={() => scrollToBottom('smooth')}
            aria-label="Прокрутить к последнему сообщению"
            title="К последнему сообщению"
            id="chat-scroll-bottom-btn"
          >
            <span>↓</span>
            <span>Вниз</span>
          </button>
        )}
      </div>

      {/* Ошибка взаимодействия */}
      {errorMessage && (
        <div className="chat-error-alert" role="alert" id="chat-error-banner">
          <span>{errorMessage}</span>
          <button
            type="button"
            className="chat-error-close"
            onClick={() => setErrorMessage(null)}
            aria-label="Закрыть ошибку"
          >
            ✕
          </button>
        </div>
      )}

      {/* 4. Форма ввода сообщения: поле ввода и кнопка "Send" */}
      <form className="chat-input-form" onSubmit={handleSend} id="chat-input-form">
        <div className="chat-input-wrapper">
          <input
            ref={inputRef}
            type="text"
            className="chat-text-input"
            placeholder={placeholder}
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            disabled={isSending}
            maxLength={maxMessageLength}
            aria-label="Текстовое поле ввода сообщения для AI"
            id="chat-user-input"
            autoComplete="off"
          />
          <button
            type="submit"
            className="btn btn-primary chat-send-btn"
            disabled={isSending || !inputMessage.trim()}
            title="Отправить сообщение (Enter)"
            id="chat-send-btn"
            aria-label="Отправить сообщение"
          >
            {isSending ? (
              <span className="btn-spinner" aria-hidden="true"></span>
            ) : (
              <>
                <span>Отправить</span>
                <svg className="send-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5"
                  />
                </svg>
              </>
            )}
          </button>
        </div>
        <footer className="chat-input-footer">
          <span className="chat-hint">Нажмите Enter для отправки. Поддерживаются вопросы по Spring Boot, REST API и коду.</span>
          <span className="chat-char-counter" aria-label={`Символов: ${inputMessage.length} из ${maxMessageLength}`}>
            {inputMessage.length} / {maxMessageLength}
          </span>
        </footer>
      </form>
    </div>
  );
}
