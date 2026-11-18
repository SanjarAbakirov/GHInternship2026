import React, { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from './AuthContext';
import { getUserChatSessions, getSessionMessages, sendChatMessage } from '../services/api';

// eslint-disable-next-line react-refresh/only-export-components
export const ChatContext = createContext(null);

/**
 * Вспомогательная функция для генерации читаемого времени HH:MM
 */
function formatTimestamp() {
  const now = new Date();
  return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

/**
 * Генерация уникального ID для сообщений
 */
function generateMessageId(prefix = 'msg') {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
}

/**
 * Создание начального приветственного сообщения
 */
function createWelcomeMessage(username) {
  return {
    id: generateMessageId('welcome'),
    sender: 'assistant',
    text: `Здравствуйте${username ? `, ${username}` : ''}! Я ваш AI-ассистент, работающий на базе защищенного Spring Boot бэкенда и PostgreSQL. Чем я могу помочь вам сегодня?`,
    timestamp: formatTimestamp(),
  };
}

/**
 * Провайдер состояния чата и истории диалогов (React Context API)
 * Отвечает за:
 * 1. Хранение active sessionId и распознавание New Chat vs Ongoing Conversation.
 * 2. Переключение бесед: вызов GET /api/chat/sessions/{sessionId}, очистка старых сообщений и загрузка истории.
 * 3. Отправку сообщений с привязкой к текущей сессии.
 * 4. Загрузку и кэширование списка сохраненных сессий пользователя.
 */
export function ChatProvider({ children, initialSessionId = null }) {
  const { token, currentUser } = useAuth();

  // Состояние текущего активного диалога
  const [currentSessionId, setCurrentSessionId] = useState(initialSessionId);
  const [currentSessionTitle, setCurrentSessionTitle] = useState(null);
  const [messages, setMessages] = useState(() => [createWelcomeMessage(currentUser?.username)]);

  // Состояние загрузки сообщений выбранной сессии
  const [isLoadingSession, setIsLoadingSession] = useState(false);
  const [sessionError, setSessionError] = useState(null);

  // Список всех сессий пользователя для сайдбара
  const [sessions, setSessions] = useState([]);
  const [isLoadingSessions, setIsLoadingSessions] = useState(false);
  const [sessionsError, setSessionsError] = useState(null);

  // Ссылка для отслеживания последнего запроса и предотвращения race conditions
  const activeRequestSequenceRef = useRef(0);
  const [reloadKey, setReloadKey] = useState(0);

  const refreshSessions = useCallback(() => {
    setReloadKey((prev) => prev + 1);
  }, []);

  // Загружаем список сессий при изменении токена (авторизация пользователя) или reloadKey
  useEffect(() => {
    if (!token) {
      queueMicrotask(() => {
        setSessions([]);
        setCurrentSessionId(null);
        setCurrentSessionTitle(null);
        setMessages([createWelcomeMessage()]);
      });
      return;
    }

    let isCancelled = false;

    const fetchSessions = async () => {
      setIsLoadingSessions(true);
      setSessionsError(null);
      console.log('[ChatContext] Загрузка списка сессий пользователя...');

      try {
        const data = await getUserChatSessions(token);
        if (!isCancelled) {
          const list = Array.isArray(data) ? data : [];
          setSessions(list);
          console.log(`[ChatContext] Загружено сессий: ${list.length}`);
        }
      } catch (err) {
        if (!isCancelled) {
          console.error('[ChatContext] Ошибка загрузки списка сессий:', err);
          let userMessage = err.message || 'Не удалось загрузить историю диалогов';
          if (err.status === 401) {
            userMessage = 'Сессия истекла. Пожалуйста, выполните вход повторно.';
          } else if (err.status >= 500) {
            userMessage = 'Сервер временно недоступен. Попробуйте обновить позже.';
          }
          setSessionsError(userMessage);
        }
      } finally {
        if (!isCancelled) {
          setIsLoadingSessions(false);
        }
      }
    };

    fetchSessions();

    return () => {
      isCancelled = true;
    };
  }, [token, reloadKey]);

  /**
   * Переключение на выбранную беседу (Conversation Switching):
   * 1. Немедленная очистка сообщений предыдущей беседы (clearing any existing messages).
   * 2. Вызов GET /api/chat/sessions/{sessionId} для загрузки полной истории.
   * 3. Установка загруженных сообщений в ленту диалога.
   * 4. Сохранение currentSessionId в состоянии для привязки последующих сообщений.
   */
  const switchConversation = useCallback(
    async (sessionId) => {
      if (!sessionId) return;
      if (Number(sessionId) === Number(currentSessionId)) {
        console.log(`[ChatContext] Сессия #${sessionId} уже активна`);
        return;
      }

      console.log(`[ChatContext] Переключение на сессию диалога [id=${sessionId}]`);
      const requestId = ++activeRequestSequenceRef.current;

      // 1. Очищаем сообщения предыдущей беседы и ставим лоадер
      setMessages([]);
      setIsLoadingSession(true);
      setSessionError(null);

      // Ищем заголовок сессии в уже загруженном списке
      const matchedSession = sessions.find((s) => Number(s.id) === Number(sessionId));
      if (matchedSession) {
        setCurrentSessionTitle(matchedSession.title);
      }

      try {
        const data = await getSessionMessages(sessionId, token);

        // Проверяем актуальность запроса (защита от гонки запросов)
        if (requestId !== activeRequestSequenceRef.current) {
          console.warn(`[ChatContext] Ответ для сессии #${sessionId} проигнорирован (устаревший запрос)`);
          return;
        }

        console.log(`[ChatContext] Получено сообщений для сессии #${sessionId}: ${data?.length || 0}`);

        if (Array.isArray(data) && data.length > 0) {
          const mappedMessages = data.map((item) => ({
            id: `hist-${item.id}`,
            sender: item.senderRole === 'user' ? 'user' : 'assistant',
            text: item.content,
            timestamp: item.timestamp
              ? new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : formatTimestamp(),
          }));
          setMessages(mappedMessages);
        } else {
          setMessages([
            {
              id: generateMessageId('welcome'),
              sender: 'assistant',
              text: `Сессия #${sessionId} открыта. Задайте вопрос, чтобы продолжить диалог.`,
              timestamp: formatTimestamp(),
            },
          ]);
        }

        // Сохраняем активный sessionId в стейте
        setCurrentSessionId(Number(sessionId));
      } catch (err) {
        if (requestId !== activeRequestSequenceRef.current) return;
        console.error(`[ChatContext] Ошибка при переключении на сессию #${sessionId}:`, err);
        setSessionError(err.message || 'Не удалось загрузить историю выбранного диалога');
        setMessages([
          {
            id: generateMessageId('err'),
            sender: 'system',
            text: `⚠️ Не удалось загрузить диалог #${sessionId}: ${err.message || 'Ошибка сети'}`,
            timestamp: formatTimestamp(),
          },
        ]);
      } finally {
        if (requestId === activeRequestSequenceRef.current) {
          setIsLoadingSession(false);
        }
      }
    },
    [currentSessionId, sessions, token]
  );

  /**
   * Начало нового диалога (New Chat):
   * Сброс currentSessionId в null, очистка сообщений и возврат к приветственному экрану.
   */
  const startNewConversation = useCallback(() => {
    console.log('[ChatContext] Начало нового диалога: сброс currentSessionId');
    activeRequestSequenceRef.current++;
    setCurrentSessionId(null);
    setCurrentSessionTitle(null);
    setIsLoadingSession(false);
    setSessionError(null);
    setMessages([createWelcomeMessage(currentUser?.username)]);
  }, [currentUser?.username]);

  /**
   * Отправка сообщения в текущий диалог:
   * Если currentSessionId задан -> отправляется в рамках ongoing conversation.
   * Если currentSessionId равен null -> создается новый диалог на бэкенде,
   * полученный chatSessionId сохраняется в стейте, и обновляется список сессий.
   */
  const sendMessage = useCallback(
    async (messageText) => {
      const trimmed = messageText.trim();
      if (!trimmed) return null;

      const userMsg = {
        id: generateMessageId('user'),
        sender: 'user',
        text: trimmed,
        timestamp: formatTimestamp(),
      };

      // Немедленное отображение сообщения пользователя
      setMessages((prev) => [...prev, userMsg]);

      try {
        console.log(
          `[ChatContext] Отправка сообщения [sessionId: ${currentSessionId || 'новый'}, длина: ${trimmed.length}]`
        );
        const responseDto = await sendChatMessage(trimmed, token, currentSessionId);

        const aiMsg = {
          id: generateMessageId('ai'),
          sender: 'assistant',
          text: responseDto.response || 'Ответ от AI успешно получен.',
          timestamp: formatTimestamp(),
        };
        setMessages((prev) => [...prev, aiMsg]);

        // Если это был новый диалог, бэкенд возвращает chatSessionId
        if (responseDto?.chatSessionId) {
          const newId = Number(responseDto.chatSessionId);
          console.log(`[ChatContext] Сессия диалога создана/подтверждена [id=${newId}]`);
          if (currentSessionId !== newId) {
            setCurrentSessionId(newId);
            // Перезагружаем список диалогов для отображения новой сессии в сайдбаре
            refreshSessions();
          }
        }

        return responseDto;
      } catch (err) {
        console.error('[ChatContext] Ошибка отправки сообщения:', err);
        const errorMsg = {
          id: generateMessageId('err'),
          sender: 'system',
          text: `⚠️ Ошибка: ${err.message || 'Не удалось отправить сообщение'}`,
          timestamp: formatTimestamp(),
        };
        setMessages((prev) => [...prev, errorMsg]);
        throw err;
      }
    },
    [currentSessionId, token, refreshSessions]
  );

  /**
   * Очистка текущего окна сообщений
   */
  const clearMessages = useCallback(() => {
    setMessages([createWelcomeMessage(currentUser?.username)]);
  }, [currentUser?.username]);

  const value = {
    // Состояние активного диалога
    currentSessionId,
    currentSessionTitle,
    isNewConversation: currentSessionId === null,
    messages,
    setMessages,
    isLoadingSession,
    sessionError,

    // Список сессий диалогов пользователя
    sessions,
    isLoadingSessions,
    sessionsError,

    // Операции управления беседами (Conversation Switching)
    switchConversation,
    startNewConversation,
    sendMessage,
    clearMessages,
    refreshSessions,
  };

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
}

/**
 * Хук для удобного доступа к состоянию чата
 */
// eslint-disable-next-line react-refresh/only-export-components
export function useChat() {
  const context = useContext(ChatContext);
  return context;
}
