import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route, useLocation } from 'react-router-dom';
import { describe, test, expect, vi, beforeEach, afterEach } from 'vitest';

// Импортируем через Chat.js (реэкспорт Chat.jsx) для проверки совместимости обоих импортов
import Chat from './Chat.js';
import ProtectedRoute from './ProtectedRoute.jsx';
import { AuthProvider, AuthContext } from '../context/AuthContext.jsx';

/**
 * Вспомогательный компонент для проверки редиректа и переданного state в интеграционных тестах ProtectedRoute
 */
function MockLoginPage() {
  const location = useLocation();
  return (
    <div data-testid="mock-login-page">
      <h1>Login Page</h1>
      {location.state?.message && (
        <div role="alert" data-testid="redirect-notification">
          {location.state.message}
        </div>
      )}
    </div>
  );
}

/**
 * Вспомогательная функция создания mock HTTP Response для fetch
 */
function createMockFetchResponse(body, { ok = true, status = 200 } = {}) {
  return {
    ok,
    status,
    headers: new Headers({ 'content-type': 'application/json' }),
    json: async () => body,
    text: async () => (typeof body === 'string' ? body : JSON.stringify(body)),
  };
}

describe('Chat Component — Unit & Integration Tests', () => {
  const mockUser = {
    username: 'sanjar_dev',
    id: 101,
    loginTime: '12:00:00',
  };
  const mockToken = 'test-jwt-token-xyz-987';

  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
    window.HTMLElement.prototype.scrollIntoView = vi.fn();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
  });

  /**
   * Вспомогательный рендер с реальным AuthProvider (данные загружаются из localStorage)
   */
  function renderWithAuthenticatedProvider(ui) {
    localStorage.setItem('gh_internship_user', JSON.stringify(mockUser));
    localStorage.setItem('gh_internship_token', mockToken);

    return render(<AuthProvider>{ui}</AuthProvider>);
  }

  test('Render Test: корректно отображает поле ввода, кнопку отправки и приветствие в авторизованном контексте', () => {
    renderWithAuthenticatedProvider(<Chat />);

    // Проверяем отображение основного контейнера и заголовка чата
    expect(document.getElementById('chat-component')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /интеллектуальный ai ассистент/i })).toBeInTheDocument();

    // Проверяем приветственное сообщение с именем пользователя
    expect(screen.getByText(/Здравствуйте, sanjar_dev!/i)).toBeInTheDocument();
    expect(screen.getByText(mockUser.username)).toBeInTheDocument();

    // Проверяем наличие области отображения сообщений
    const messagesDisplay = screen.getByRole('log', { name: /история сообщений/i });
    expect(messagesDisplay).toBeInTheDocument();

    // Проверяем наличие поля ввода текста
    const inputField = screen.getByLabelText(/текстовое поле ввода сообщения для ai/i);
    expect(inputField).toBeInTheDocument();
    expect(inputField).toHaveValue('');
    expect(inputField).not.toBeDisabled();

    // Проверяем кнопку "Отправить" (Send) и её неактивное состояние при пустом вводе
    const sendButton = screen.getByRole('button', { name: /отправить сообщение/i });
    expect(sendButton).toBeInTheDocument();
    expect(sendButton).toBeDisabled();
  });

  test('Interaction & API Mocking Test: ввод текста, клик Send, проверка payload/заголовка Authorization и отображение ответа AI', async () => {
    const user = userEvent.setup();

    const mockFetch = vi.fn().mockResolvedValueOnce(
      createMockFetchResponse({
        response: 'Spring Boot — это мощный Java-фреймворк для быстрого создания микросервисов.',
        timestamp: '2026-11-10T22:30:00Z',
      })
    );
    vi.stubGlobal('fetch', mockFetch);

    renderWithAuthenticatedProvider(<Chat />);

    const inputField = screen.getByLabelText(/текстовое поле ввода сообщения для ai/i);
    const sendButton = screen.getByRole('button', { name: /отправить сообщение/i });

    // Симулируем ввод текста пользователем
    const testMessage = 'Расскажи про Spring Boot';
    await user.type(inputField, testMessage);

    expect(inputField).toHaveValue(testMessage);
    expect(sendButton).toBeEnabled();

    // Нажимаем кнопку отправки
    await user.click(sendButton);

    // 1. Проверяем, что сообщение пользователя мгновенно появилось в чате, а поле ввода очистилось
    expect(screen.getByText(testMessage)).toBeInTheDocument();
    expect(inputField).toHaveValue('');

    // 2. Проверяем вызов fetch с правильным URL, методом, payload и заголовком Authorization
    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledTimes(1);
    });

    const [calledUrl, calledOptions] = mockFetch.mock.calls[0];
    expect(calledUrl).toMatch(/\/api\/chat$/);
    expect(calledOptions.method).toBe('POST');
    expect(calledOptions.headers).toMatchObject({
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Authorization': `Bearer ${mockToken}`,
    });
    expect(JSON.parse(calledOptions.body)).toEqual({ message: testMessage });

    // 3. Проверяем, что замоканный ответ AI корректно отобразился в ленте сообщений
    const aiReply = await screen.findByText(
      'Spring Boot — это мощный Java-фреймворк для быстрого создания микросервисов.'
    );
    expect(aiReply).toBeInTheDocument();

    // 4. Проверяем вызов автопрокрутки к последнему сообщению
    await waitFor(() => {
      expect(window.HTMLElement.prototype.scrollIntoView).toHaveBeenCalled();
    });
  });

  test('Unique Keys & Sequential Messages: последовательные сообщения получают уникальные React-ключи без коллизий', async () => {
    const user = userEvent.setup();

    const mockFetch = vi
      .fn()
      .mockResolvedValueOnce(createMockFetchResponse({ response: 'Ответ #1' }))
      .mockResolvedValueOnce(createMockFetchResponse({ response: 'Ответ #2' }));
    vi.stubGlobal('fetch', mockFetch);

    renderWithAuthenticatedProvider(<Chat />);

    const inputField = screen.getByLabelText(/текстовое поле ввода сообщения для ai/i);
    const sendButton = screen.getByRole('button', { name: /отправить сообщение/i });

    await user.type(inputField, 'Первый вопрос');
    await user.click(sendButton);
    expect(await screen.findByText('Ответ #1')).toBeInTheDocument();

    await user.type(inputField, 'Второй вопрос');
    await user.click(sendButton);
    expect(await screen.findByText('Ответ #2')).toBeInTheDocument();

    // Проверяем уникальность атрибута data-message-id у всех отрисованных сообщений
    const renderedMessages = document.querySelectorAll('[data-message-id]');
    const messageIds = Array.from(renderedMessages).map((el) => el.getAttribute('data-message-id'));
    // 1 welcome + 2 user + 2 AI = 5 сообщений
    expect(messageIds).toHaveLength(5);
    expect(new Set(messageIds).size).toBe(5);
  });

  test('Error Handling (401 Unauthorized): отображает сообщение об ошибке авторизации при отклонении токена сервером', async () => {
    const user = userEvent.setup();

    const mockFetch = vi.fn().mockResolvedValueOnce(
      createMockFetchResponse(
        { error: 'Unauthorized', message: 'Invalid or expired token' },
        { ok: false, status: 401 }
      )
    );
    vi.stubGlobal('fetch', mockFetch);

    // Передаем контекст напрямую через AuthContext.Provider для демонстрации изоляции модульного теста
    const customAuthContext = {
      currentUser: mockUser,
      token: 'expired-token-000',
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    };

    render(
      <AuthContext.Provider value={customAuthContext}>
        <Chat />
      </AuthContext.Provider>
    );

    const inputField = screen.getByLabelText(/текстовое поле ввода сообщения для ai/i);
    const sendButton = screen.getByRole('button', { name: /отправить сообщение/i });

    await user.type(inputField, 'Проверка 401');
    await user.click(sendButton);

    // Проверяем, что баннер ошибки и системное предупреждение отобразились
    const errorBanner = await screen.findByRole('alert');
    expect(errorBanner).toHaveTextContent(/401 Unauthorized/i);
    expect(screen.getByText(/⚠️ Ошибка:.*401 Unauthorized/i)).toBeInTheDocument();

    // Поле ввода снова активно после ошибки
    expect(inputField).not.toBeDisabled();
  });

  test('Quick Prompts & Clear Chat: подстановка быстрого вопроса и очистка истории диалога', async () => {
    const user = userEvent.setup();
    renderWithAuthenticatedProvider(<Chat />);

    const inputField = screen.getByLabelText(/текстовое поле ввода сообщения для ai/i);
    const quickChip = screen.getByRole('button', { name: /Что такое Spring Boot/i });

    await user.click(quickChip);
    expect(inputField).toHaveValue('Что такое Spring Boot и какие у него преимущества?');

    const clearBtn = screen.getByRole('button', { name: /очистить диалог/i });
    await user.click(clearBtn);

    expect(screen.getByText(/История диалога очищена/i)).toBeInTheDocument();
  });

  test('ProtectedRoute Integration: редиректит неавторизованного пользователя на /login с уведомлением и пускает авторизованного в Chat', () => {
    // 1. Неавторизованный пользователь -> перенаправление на /login с сообщением
    const { unmount } = render(
      <MemoryRouter initialEntries={['/chat']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<MockLoginPage />} />
            <Route
              path="/chat"
              element={
                <ProtectedRoute>
                  <Chat />
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.getByTestId('mock-login-page')).toBeInTheDocument();
    expect(screen.getByTestId('redirect-notification')).toHaveTextContent(
      'You must be logged in to access the chat.'
    );

    unmount();

    // 2. Авторизованный пользователь -> рендерится компонент Chat
    localStorage.setItem('gh_internship_user', JSON.stringify(mockUser));
    localStorage.setItem('gh_internship_token', mockToken);

    render(
      <MemoryRouter initialEntries={['/chat']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<MockLoginPage />} />
            <Route
              path="/chat"
              element={
                <ProtectedRoute>
                  <Chat />
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.queryByTestId('mock-login-page')).not.toBeInTheDocument();
    expect(screen.getByLabelText(/текстовое поле ввода сообщения для ai/i)).toBeInTheDocument();
  });
});
