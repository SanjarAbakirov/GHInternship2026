import { describe, test, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  getStoredAuthToken,
  authFetch,
  apiClient,
  sendChatMessage,
  getUserChatSessions,
  getSessionMessages,
} from './api';

describe('API Client & Authenticated Fetch Unit Tests', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
  });

  test('getStoredAuthToken возвращает null, если токен отсутствует в localStorage', () => {
    expect(getStoredAuthToken()).toBeNull();
    localStorage.setItem('gh_internship_token', 'jwt-123');
    expect(getStoredAuthToken()).toBe('jwt-123');
  });

  test('authFetch и apiClient.post автоматически прикрепляют заголовок Authorization: Bearer <token> из localStorage', async () => {
    localStorage.setItem('gh_internship_token', 'stored-jwt-abc-999');

    const mockFetch = vi.fn().mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({ response: 'OK from backend' }),
    });
    vi.stubGlobal('fetch', mockFetch);

    const res = await sendChatMessage('Test message');
    expect(res).toEqual({ response: 'OK from backend' });

    expect(mockFetch).toHaveBeenCalledTimes(1);
    const [url, options] = mockFetch.mock.calls[0];
    expect(url).toMatch(/\/api\/chat$/);
    expect(options.method).toBe('POST');
    expect(options.headers['Authorization']).toBe('Bearer stored-jwt-abc-999');
    expect(options.headers['Content-Type']).toBe('application/json');
    expect(JSON.parse(options.body)).toEqual({ message: 'Test message' });
  });

  test('authFetch не добавляет заголовок Authorization, если токен отсутствует', async () => {
    const mockFetch = vi.fn().mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({ status: 'public' }),
    });
    vi.stubGlobal('fetch', mockFetch);

    await authFetch('/api/public');

    const [, options] = mockFetch.mock.calls[0];
    expect(options.headers['Authorization']).toBeUndefined();
  });

  test('apiClient выбрасывает структурированную ошибку при HTTP 401 Unauthorized', async () => {
    const mockFetch = vi.fn().mockResolvedValueOnce({
      ok: false,
      status: 401,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({
        status: 401,
        error: 'Unauthorized',
        message: 'Full authentication is required',
      }),
    });
    vi.stubGlobal('fetch', mockFetch);

    await expect(apiClient.post('/api/chat', { message: 'hi' })).rejects.toMatchObject({
      status: 401,
      message: 'Full authentication is required',
    });
  });

  test('sendChatMessage передает chatSessionId в теле запроса, если указан', async () => {
    const mockFetch = vi.fn().mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({ chatSessionId: 101, response: 'Session continued' }),
    });
    vi.stubGlobal('fetch', mockFetch);

    const res = await sendChatMessage('Continuing conversation', 'jwt-token-xyz', 101);
    expect(res).toEqual({ chatSessionId: 101, response: 'Session continued' });

    const [, options] = mockFetch.mock.calls[0];
    expect(options.headers['Authorization']).toBe('Bearer jwt-token-xyz');
    expect(JSON.parse(options.body)).toEqual({
      message: 'Continuing conversation',
      chatSessionId: 101,
    });
  });

  test('getUserChatSessions запрашивает GET /api/chat/sessions с токеном авторизации', async () => {
    const mockSessions = [
      { id: 1, title: 'Диалог 1', createdAt: '2026-11-16T12:00:00Z', updatedAt: '2026-11-16T12:05:00Z', messageCount: 2 },
    ];
    const mockFetch = vi.fn().mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => mockSessions,
    });
    vi.stubGlobal('fetch', mockFetch);

    const result = await getUserChatSessions('custom-jwt-token');
    expect(result).toEqual(mockSessions);

    const [url, options] = mockFetch.mock.calls[0];
    expect(url).toMatch(/\/api\/chat\/sessions$/);
    expect(options.method).toBe('GET');
    expect(options.headers['Authorization']).toBe('Bearer custom-jwt-token');
  });

  test('getSessionMessages запрашивает GET /api/chat/sessions/{sessionId} с токеном', async () => {
    const mockMessages = [
      { id: 10, sessionId: 42, senderRole: 'user', content: 'Привет', timestamp: '2026-11-16T12:00:00Z' },
      { id: 11, sessionId: 42, senderRole: 'ai', content: 'Привет!', timestamp: '2026-11-16T12:00:05Z' },
    ];
    const mockFetch = vi.fn().mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => mockMessages,
    });
    vi.stubGlobal('fetch', mockFetch);

    const result = await getSessionMessages(42, 'custom-jwt-token');
    expect(result).toEqual(mockMessages);

    const [url, options] = mockFetch.mock.calls[0];
    expect(url).toMatch(/\/api\/chat\/sessions\/42$/);
    expect(options.method).toBe('GET');
    expect(options.headers['Authorization']).toBe('Bearer custom-jwt-token');
  });
});

