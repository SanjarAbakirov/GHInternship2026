import { describe, test, expect, vi, beforeEach, afterEach } from 'vitest';
import { getStoredAuthToken, authFetch, apiClient, sendChatMessage } from './api';

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
});

