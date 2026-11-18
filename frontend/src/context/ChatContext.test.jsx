import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach, afterEach } from 'vitest';
import { AuthContext } from './AuthContext';
import { ChatProvider, useChat } from './ChatContext';
import * as api from '../services/api';

// Мокаем API-клиент
vi.mock('../services/api', () => ({
  getUserChatSessions: vi.fn(),
  getSessionMessages: vi.fn(),
  sendChatMessage: vi.fn(),
}));

/**
 * Тестовый компонент-потребитель ChatContext
 */
function TestChatConsumer() {
  const {
    currentSessionId,
    currentSessionTitle,
    isNewConversation,
    messages,
    isLoadingSession,
    sessionError,
    sessions,
    switchConversation,
    startNewConversation,
    sendMessage,
  } = useChat();

  return (
    <div>
      <div data-testid="session-id">{currentSessionId !== null ? currentSessionId : 'null'}</div>
      <div data-testid="is-new-conv">{isNewConversation ? 'yes' : 'no'}</div>
      <div data-testid="session-title">{currentSessionTitle || 'none'}</div>
      <div data-testid="is-loading-session">{isLoadingSession ? 'loading' : 'idle'}</div>
      <div data-testid="session-error">{sessionError || 'none'}</div>
      <div data-testid="sessions-count">{sessions.length}</div>
      <div data-testid="messages-count">{messages.length}</div>

      <ul data-testid="messages-list">
        {messages.map((m) => (
          <li key={m.id} data-testid="message-item">
            {m.sender}: {m.text}
          </li>
        ))}
      </ul>

      <button
        type="button"
        data-testid="btn-switch-42"
        onClick={() => switchConversation(42)}
      >
        Переключить на 42
      </button>

      <button
        type="button"
        data-testid="btn-switch-99"
        onClick={() => switchConversation(99)}
      >
        Переключить на 99
      </button>

      <button
        type="button"
        data-testid="btn-new-chat"
        onClick={startNewConversation}
      >
        Новый чат
      </button>

      <button
        type="button"
        data-testid="btn-send-msg"
        onClick={() => sendMessage('Тестовый вопрос')}
      >
        Отправить
      </button>
    </div>
  );
}

function renderWithProviders(initialSessionId = null, token = 'valid-jwt-token') {
  const mockAuthValue = {
    currentUser: { username: 'test_user', id: 1 },
    token,
    isLoading: false,
    login: vi.fn(),
    logout: vi.fn(),
  };

  return render(
    <AuthContext.Provider value={mockAuthValue}>
      <ChatProvider initialSessionId={initialSessionId}>
        <TestChatConsumer />
      </ChatProvider>
    </AuthContext.Provider>
  );
}

describe('ChatContext & Conversation Switching — Unit Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    api.getUserChatSessions.mockResolvedValue([
      { id: 42, title: 'Архитектура микросервисов', messageCount: 2 },
      { id: 99, title: 'Базы данных PostgreSQL', messageCount: 3 },
    ]);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  test('Начальное состояние: новый диалог, currentSessionId равен null', async () => {
    renderWithProviders();

    expect(screen.getByTestId('session-id')).toHaveTextContent('null');
    expect(screen.getByTestId('is-new-conv')).toHaveTextContent('yes');
    expect(screen.getByTestId('is-loading-session')).toHaveTextContent('idle');

    // Проверяем, что загрузился список сессий для сайдбара
    await waitFor(() => {
      expect(screen.getByTestId('sessions-count')).toHaveTextContent('2');
    });
    expect(api.getUserChatSessions).toHaveBeenCalledWith('valid-jwt-token');
  });

  test('switchConversation(sessionId): очищает старые сообщения, вызывает GET /api/chat/sessions/{id} и сохраняет currentSessionId', async () => {
    const user = userEvent.setup();

    const mockMessages = [
      { id: 1, sessionId: 42, senderRole: 'user', content: 'Что такое CQRS?' },
      { id: 2, sessionId: 42, senderRole: 'ai', content: 'CQRS разделяет команды и запросы.' },
    ];
    api.getSessionMessages.mockResolvedValue(mockMessages);

    renderWithProviders();

    // Дожидаемся загрузки сессий
    await waitFor(() => {
      expect(screen.getByTestId('sessions-count')).toHaveTextContent('2');
    });

    // Кликаем переключение на сессию 42
    await user.click(screen.getByTestId('btn-switch-42'));

    // Проверяем, что был вызван API с правильным ID и токеном
    expect(api.getSessionMessages).toHaveBeenCalledWith(42, 'valid-jwt-token');

    // Проверяем состояние после переключения
    await waitFor(() => {
      expect(screen.getByTestId('session-id')).toHaveTextContent('42');
      expect(screen.getByTestId('is-new-conv')).toHaveTextContent('no');
      expect(screen.getByTestId('session-title')).toHaveTextContent('Архитектура микросервисов');
      expect(screen.getByTestId('messages-count')).toHaveTextContent('2');
    });

    expect(screen.getByText(/Что такое CQRS\?/)).toBeInTheDocument();
    expect(screen.getByText(/CQRS разделяет команды и запросы\./)).toBeInTheDocument();
  });

  test('startNewConversation(): сбрасывает currentSessionId в null и очищает диалог', async () => {
    const user = userEvent.setup();

    api.getSessionMessages.mockResolvedValue([
      { id: 10, sessionId: 42, senderRole: 'user', content: 'Вопрос' },
    ]);

    renderWithProviders(42);

    // Переключаемся в новый чат
    await user.click(screen.getByTestId('btn-new-chat'));

    expect(screen.getByTestId('session-id')).toHaveTextContent('null');
    expect(screen.getByTestId('is-new-conv')).toHaveTextContent('yes');
    expect(screen.getByTestId('session-title')).toHaveTextContent('none');
    expect(screen.getByTestId('messages-count')).toHaveTextContent('1'); // Приветственное сообщение
  });

  test('sendMessage привязывает текущий sessionId к запросу', async () => {
    const user = userEvent.setup();

    api.sendChatMessage.mockResolvedValue({
      response: 'Ответ AI на вопрос',
      chatSessionId: 42,
    });

    renderWithProviders(42);

    await user.click(screen.getByTestId('btn-send-msg'));

    expect(api.sendChatMessage).toHaveBeenCalledWith('Тестовый вопрос', 'valid-jwt-token', 42);
    await waitFor(() => {
      expect(screen.getByText(/Ответ AI на вопрос/)).toBeInTheDocument();
    });
  });

  test('sendMessage в новом диалоге сохраняет полученный chatSessionId и обновляет список сессий', async () => {
    const user = userEvent.setup();

    api.sendChatMessage.mockResolvedValue({
      response: 'Новый ответ',
      chatSessionId: 888,
    });

    renderWithProviders(null);

    await user.click(screen.getByTestId('btn-send-msg'));

    expect(api.sendChatMessage).toHaveBeenCalledWith('Тестовый вопрос', 'valid-jwt-token', null);

    await waitFor(() => {
      expect(screen.getByTestId('session-id')).toHaveTextContent('888');
      expect(screen.getByTestId('is-new-conv')).toHaveTextContent('no');
    });

    // Проверяем, что перезапросился список сессий
    expect(api.getUserChatSessions).toHaveBeenCalledTimes(2);
  });

  test('Повторный клик по уже активной сессии не делает повторный запрос', async () => {
    const user = userEvent.setup();

    api.getSessionMessages.mockResolvedValue([
      { id: 1, sessionId: 42, senderRole: 'user', content: 'Привет' },
    ]);

    renderWithProviders();

    // Первый клик по сессии 42
    await user.click(screen.getByTestId('btn-switch-42'));
    await waitFor(() => {
      expect(screen.getByTestId('session-id')).toHaveTextContent('42');
    });
    expect(api.getSessionMessages).toHaveBeenCalledTimes(1);

    // Повторный клик по сессии 42
    await user.click(screen.getByTestId('btn-switch-42'));
    // Количество вызовов не изменилось
    expect(api.getSessionMessages).toHaveBeenCalledTimes(1);
  });
});
