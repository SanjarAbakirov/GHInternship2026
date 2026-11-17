import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, test, expect, vi, beforeEach, afterEach } from 'vitest';
import ChatSidebar from './ChatSidebar';
import { AuthProvider } from '../context/AuthContext';

function createMockFetchResponse(body, { ok = true, status = 200 } = {}) {
  return {
    ok,
    status,
    headers: new Headers({ 'content-type': 'application/json' }),
    json: async () => body,
    text: async () => (typeof body === 'string' ? body : JSON.stringify(body)),
  };
}

describe('ChatSidebar Component — Unit & Integration Tests', () => {
  const mockUser = { username: 'test_dev', id: 101 };
  const mockToken = 'mock-jwt-token-123';

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('gh_internship_user', JSON.stringify(mockUser));
    localStorage.setItem('gh_internship_token', mockToken);
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
  });

  function renderSidebar(props = {}) {
    return render(
      <AuthProvider>
        <ChatSidebar {...props} />
      </AuthProvider>
    );
  }

  test('Render & Loading State: при загрузке отображает индикатор загрузки и делает запрос к GET /api/chat/sessions', async () => {
    let resolvePromise;
    const pendingPromise = new Promise((resolve) => {
      resolvePromise = resolve;
    });

    const mockFetch = vi.fn().mockImplementationOnce(() => pendingPromise);
    vi.stubGlobal('fetch', mockFetch);

    renderSidebar();

    // 1. Проверяем наличие индикатора загрузки
    expect(screen.getByTestId('sidebar-loading')).toBeInTheDocument();
    expect(screen.getByText(/загрузка истории/i)).toBeInTheDocument();

    // Разрешаем промис
    resolvePromise(
      createMockFetchResponse([
        { id: 1, title: 'Архитектура Spring Boot', createdAt: '2026-11-17T12:00:00Z', messageCount: 4 },
      ])
    );

    // 2. После загрузки индикатор исчезает
    await waitFor(() => {
      expect(screen.queryByTestId('sidebar-loading')).not.toBeInTheDocument();
    });

    expect(mockFetch).toHaveBeenCalledTimes(1);
    const [calledUrl, calledOptions] = mockFetch.mock.calls[0];
    expect(calledUrl).toMatch(/\/api\/chat\/sessions$/);
    expect(calledOptions.headers['Authorization']).toBe(`Bearer ${mockToken}`);
  });

  test('Render Conversation Titles as Clickable Links: отображает список диалогов в виде кликабельных ссылок и вызывает onSelectSession', async () => {
    const user = userEvent.setup();
    const mockSessions = [
      {
        id: 101,
        title: 'Как работает BCrypt?',
        createdAt: '2026-11-17T10:00:00Z',
        updatedAt: '2026-11-17T10:05:00Z',
        messageCount: 3,
        lastMessagePreview: 'BCrypt использует адаптивную соль...',
      },
      {
        id: 102,
        title: 'Что такое PostgreSQL ACID?',
        createdAt: '2026-11-17T11:00:00Z',
        updatedAt: '2026-11-17T11:08:00Z',
        messageCount: 2,
        lastMessagePreview: 'ACID гарантирует атомарность и согласованность...',
      },
    ];

    const mockFetch = vi.fn().mockResolvedValueOnce(createMockFetchResponse(mockSessions));
    vi.stubGlobal('fetch', mockFetch);

    const onSelectSessionMock = vi.fn();
    renderSidebar({ activeSessionId: 101, onSelectSession: onSelectSessionMock });

    // 1. Ждем отображения тем бесед
    const link1 = await screen.findByRole('link', { name: /как работает bcrypt/i });
    const link2 = await screen.findByRole('link', { name: /что такое postgresql acid/i });

    expect(link1).toBeInTheDocument();
    expect(link2).toBeInTheDocument();

    // 2. Проверяем активную подсветку активной сессии (101)
    expect(link1).toHaveClass('session-link-active');
    expect(link1).toHaveAttribute('aria-current', 'true');
    expect(link2).not.toHaveClass('session-link-active');

    // 3. Проверяем клик по ссылке второй сессии
    await user.click(link2);
    expect(onSelectSessionMock).toHaveBeenCalledTimes(1);
    expect(onSelectSessionMock).toHaveBeenCalledWith(102);
  });

  test('Error State & Retry: отображает сообщение об ошибке при сбое API и повторяет запрос при клике на "Повторить"', async () => {
    const user = userEvent.setup();

    // Первый вызов падает с 500
    const mockFetch = vi
      .fn()
      .mockResolvedValueOnce(createMockFetchResponse({ message: 'Ошибка базы данных' }, { ok: false, status: 500 }))
      .mockResolvedValueOnce(
        createMockFetchResponse([
          { id: 201, title: 'Восстановленный диалог', createdAt: '2026-11-17T12:00:00Z', messageCount: 1 },
        ])
      );
    vi.stubGlobal('fetch', mockFetch);

    renderSidebar();

    // 1. Проверяем отображение блока ошибки
    const errorAlert = await screen.findByTestId('sidebar-error');
    expect(errorAlert).toBeInTheDocument();
    expect(screen.getByText(/не удалось загрузить историю/i)).toBeInTheDocument();

    // 2. Нажимаем кнопку повтора запроса
    const retryBtn = screen.getByRole('button', { name: /повторить загрузку истории диалогов/i });
    await user.click(retryBtn);

    // 3. Проверяем успешную повторную загрузку
    expect(await screen.findByRole('link', { name: /восстановленный диалог/i })).toBeInTheDocument();
    expect(mockFetch).toHaveBeenCalledTimes(2);
  });

  test('Empty State: отображает дружелюбное сообщение, если у пользователя еще нет сохраненных сессий', async () => {
    const mockFetch = vi.fn().mockResolvedValueOnce(createMockFetchResponse([]));
    vi.stubGlobal('fetch', mockFetch);

    renderSidebar();

    const emptyState = await screen.findByTestId('sidebar-empty');
    expect(emptyState).toBeInTheDocument();
    expect(screen.getByText(/нет сохраненных диалогов/i)).toBeInTheDocument();
  });

  test('New Chat Button: клик по кнопке "+ Новый диалог" вызывает onNewChat', async () => {
    const user = userEvent.setup();
    const mockFetch = vi.fn().mockResolvedValueOnce(createMockFetchResponse([]));
    vi.stubGlobal('fetch', mockFetch);

    const onNewChatMock = vi.fn();
    renderSidebar({ onNewChat: onNewChatMock });

    const newChatBtn = screen.getByTestId('new-chat-button');
    await user.click(newChatBtn);

    expect(onNewChatMock).toHaveBeenCalledTimes(1);
  });

  test('Search Filter: фильтрует отображаемые диалоги по введенному ключевому слову', async () => {
    const user = userEvent.setup();
    const mockSessions = [
      { id: 1, title: 'React ProtectedRoute', createdAt: '2026-11-17T10:00:00Z', messageCount: 2 },
      { id: 2, title: 'Docker Compose Postgres', createdAt: '2026-11-17T11:00:00Z', messageCount: 5 },
    ];
    const mockFetch = vi.fn().mockResolvedValueOnce(createMockFetchResponse(mockSessions));
    vi.stubGlobal('fetch', mockFetch);

    renderSidebar();

    expect(await screen.findByRole('link', { name: /react protectedroute/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /docker compose postgres/i })).toBeInTheDocument();

    // Вводим в поиск "Docker"
    const searchInput = screen.getByLabelText(/поиск по сохраненным диалогам/i);
    await user.type(searchInput, 'Docker');

    // Проверяем, что отображается только подходящая сессия
    expect(screen.queryByRole('link', { name: /react protectedroute/i })).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: /docker compose postgres/i })).toBeInTheDocument();
  });
});

