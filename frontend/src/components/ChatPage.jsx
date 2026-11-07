import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { sendChatMessage } from '../services/api';

/**
 * Страница AI-чата (ChatPage).
 * Доступна исключительно аутентифицированным пользователям через ProtectedRoute.
 */
export default function ChatPage() {
  const { currentUser, token } = useAuth();
  const [messages, setMessages] = useState(() => [
    {
      id: 'welcome-msg',
      sender: 'assistant',
      text: `Здравствуйте, ${currentUser?.username || 'пользователь'}! Я интеллектуальный AI-ассистент платформы GHInternship. Чем я могу помочь вам сегодня?`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputMessage, setInputMessage] = useState('');
  const [isSending, setIsSending] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  // Автоматический скролл к последнему сообщению
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isSending]);

  // Фокус на поле ввода при монтировании
  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  const handleSendMessage = async (e) => {
    if (e) e.preventDefault();
    const trimmed = inputMessage.trim();
    if (!trimmed || isSending) return;

    const userMessageItem = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: trimmed,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMessageItem]);
    setInputMessage('');
    setIsSending(true);
    setErrorMessage(null);

    try {
      console.log(`[ChatPage] Отправка сообщения от ${currentUser?.username}: "${trimmed}"`);
      const responseDto = await sendChatMessage(trimmed, token);

      const aiMessageItem = {
        id: `ai-${Date.now()}`,
        sender: 'assistant',
        text: responseDto.response || 'Ответ от AI успешно получен.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };

      setMessages((prev) => [...prev, aiMessageItem]);
    } catch (err) {
      console.error('[ChatPage] Ошибка при отправке сообщения в AI чат:', err);
      const displayError = err.message || 'Не удалось получить ответ от AI-сервиса. Попробуйте еще раз.';
      setErrorMessage(displayError);

      const errorReplyItem = {
        id: `err-${Date.now()}`,
        sender: 'system',
        text: `⚠️ Ошибка: ${displayError}`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorReplyItem]);
    } finally {
      setIsSending(false);
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  };

  const handleQuickPrompt = (promptText) => {
    setInputMessage(promptText);
    setTimeout(() => {
      inputRef.current?.focus();
    }, 50);
  };

  const handleClearHistory = () => {
    setMessages([
      {
        id: `welcome-msg-${Date.now()}`,
        sender: 'assistant',
        text: `История диалога очищена. Готов к новым вопросам, ${currentUser?.username || 'пользователь'}!`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
    setErrorMessage(null);
  };

  return (
    <div className="chat-page-container" id="chat-page-view">
      {/* Шапка чата */}
      <div className="chat-header-card">
        <div className="chat-header-left">
          <div className="ai-avatar-glow">
            <svg className="ai-sparkle-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 00-2.456 2.456z" />
            </svg>
          </div>
          <div className="ai-header-info">
            <div className="ai-title-row">
              <h2 className="ai-header-title">Интеллектуальный AI Ассистент</h2>
              <span className="ai-model-tag">Spring Boot &bull; POST /api/chat</span>
            </div>
            <p className="ai-header-sub">
              Авторизован как: <strong className="user-highlight">{currentUser?.username}</strong> &bull; Защищенный сеанс
            </p>
          </div>
        </div>

        <div className="chat-header-actions">
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={handleClearHistory}
            title="Очистить историю диалога"
            id="clear-chat-history-btn"
          >
            Очистить диалог
          </button>
        </div>
      </div>

      {/* Быстрые подсказки-промпты */}
      <div className="quick-prompts-container" aria-label="Быстрые вопросы">
        <span className="quick-prompts-label">Быстрые подсказки:</span>
        <div className="quick-prompts-scroll">
          <button
            type="button"
            className="quick-chip"
            onClick={() => handleQuickPrompt('Что такое Spring Boot и какие у него преимущества?')}
          >
            💡 Что такое Spring Boot?
          </button>
          <button
            type="button"
            className="quick-chip"
            onClick={() => handleQuickPrompt('Как работает хэширование паролей с помощью BCrypt?')}
          >
            🔐 Как устроен BCrypt?
          </button>
          <button
            type="button"
            className="quick-chip"
            onClick={() => handleQuickPrompt('Объясни, как работает Protected Route в React.')}
          >
            🛡️ Как работает Protected Route?
          </button>
        </div>
      </div>

      {/* Окно сообщений */}
      <div className="chat-messages-container" role="log" aria-live="polite">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`chat-bubble-wrapper ${
              msg.sender === 'user'
                ? 'chat-bubble-user'
                : msg.sender === 'system'
                ? 'chat-bubble-system'
                : 'chat-bubble-ai'
            }`}
          >
            <div className="chat-bubble-avatar" aria-hidden="true">
              {msg.sender === 'user' ? (
                currentUser?.username?.charAt(0).toUpperCase() || 'U'
              ) : msg.sender === 'system' ? (
                '⚠️'
              ) : (
                '🤖'
              )}
            </div>
            <div className="chat-bubble-content">
              <div className="chat-bubble-meta">
                <span className="chat-sender-name">
                  {msg.sender === 'user'
                    ? currentUser?.username || 'Вы'
                    : msg.sender === 'system'
                    ? 'Системное предупреждение'
                    : 'AI Ассистент'}
                </span>
                <span className="chat-message-time">{msg.timestamp}</span>
              </div>
              <div className="chat-bubble-text">{msg.text}</div>
            </div>
          </div>
        ))}

        {/* Анимация набора текста AI */}
        {isSending && (
          <div className="chat-bubble-wrapper chat-bubble-ai">
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

        <div ref={messagesEndRef} />
      </div>

      {/* Ошибка отправки */}
      {errorMessage && (
        <div className="chat-error-alert" role="alert">
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

      {/* Панель ввода сообщения */}
      <form className="chat-input-form" onSubmit={handleSendMessage}>
        <div className="chat-input-wrapper">
          <input
            ref={inputRef}
            type="text"
            className="chat-text-input"
            placeholder="Задайте вопрос или напишите сообщение AI-ассистенту..."
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            disabled={isSending}
            maxLength={2000}
            aria-label="Поле ввода сообщения для AI"
            id="chat-user-input"
          />
          <button
            type="submit"
            className="btn btn-primary chat-send-btn"
            disabled={isSending || !inputMessage.trim()}
            title="Отправить сообщение (Enter)"
            id="chat-send-btn"
          >
            {isSending ? (
              <span className="btn-spinner"></span>
            ) : (
              <>
                <span>Отправить</span>
                <svg className="send-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5" />
                </svg>
              </>
            )}
          </button>
        </div>
        <div className="chat-input-footer">
          <span className="chat-hint">Нажмите Enter для отправки. Поддерживаются вопросы по коду, Spring Boot и архитектуре.</span>
          <span className="chat-char-counter">{inputMessage.length} / 2000</span>
        </div>
      </form>
    </div>
  );
}
