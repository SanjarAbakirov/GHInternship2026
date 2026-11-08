/**
 * API клиент для взаимодействия со Spring Boot бэкендом
 */

const STORAGE_KEY_TOKEN = 'gh_internship_token';
const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

/**
 * Получение текущего токена аутентификации из localStorage
 * @returns {string|null}
 */
export function getStoredAuthToken() {
  try {
    return localStorage.getItem(STORAGE_KEY_TOKEN) || null;
  } catch (err) {
    console.warn('[API] Не удалось прочитать токен из localStorage:', err);
    return null;
  }
}

/**
 * Переиспользуемая функция для выполнения авторизованных HTTP-запросов (authFetch).
 * Автоматически прикрепляет заголовок Authorization: Bearer <token>,
 * устанавливает заголовки Content-Type и Accept, а также централизованно обрабатывает ошибки.
 *
 * @param {string} endpoint - Путь к ресурсу API (например, '/api/chat')
 * @param {Object} [options={}] - Параметры запроса (method, headers, body, token)
 * @returns {Promise<any>}
 */
export async function authFetch(endpoint, options = {}) {
  const { token: customToken, headers: customHeaders = {}, body, ...restOptions } = options;

  // Автоматическое извлечение токена: из параметров или из хранилища localStorage
  const token = customToken !== undefined ? customToken : getStoredAuthToken();

  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    ...customHeaders,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint}`;

  console.log(`[API] Авторизованный запрос: ${options.method || 'GET'} ${url} [Auth: ${token ? 'Bearer токен прикреплен' : 'Без токена'}]`);

  const config = {
    ...restOptions,
    headers,
  };

  if (body !== undefined) {
    config.body = typeof body === 'string' ? body : JSON.stringify(body);
  }

  const response = await fetch(url, config);

  if (response.status === 401) {
    console.warn('[API] Запрос отклонен: 401 Unauthorized (сессия недействительна или отсутствует токен).');
  }

  return handleResponse(response);
}

/**
 * Переиспользуемый HTTP-клиент (apiClient), автоматически прикрепляющий токен к запросам
 */
export const apiClient = {
  get: (endpoint, options = {}) => authFetch(endpoint, { ...options, method: 'GET' }),
  post: (endpoint, body, options = {}) => authFetch(endpoint, { ...options, method: 'POST', body }),
  put: (endpoint, body, options = {}) => authFetch(endpoint, { ...options, method: 'PUT', body }),
  delete: (endpoint, options = {}) => authFetch(endpoint, { ...options, method: 'DELETE' }),
};

/**
 * Парсинг ответа от сервера с обработкой ошибок
 */
async function handleResponse(response) {
  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    let errorMessage = 'Произошла непредвиденная ошибка';
    let errorDetails = null;

    if (typeof data === 'object' && data !== null) {
      errorMessage = data.message || data.error || `Ошибка сервера (${response.status})`;
      errorDetails = data.validationErrors || data.details || null;
    } else if (typeof data === 'string' && data.length > 0) {
      errorMessage = data;
    }

    const error = new Error(errorMessage);
    error.status = response.status;
    error.details = errorDetails;
    error.validationErrors = (typeof data === 'object' && data !== null) ? (data.validationErrors || null) : null;
    error.raw = data;
    console.error(`[API] Ошибка запроса (${response.status}):`, errorMessage, errorDetails || '');
    throw error;
  }

  return data;
}

/**
 * Регистрация нового пользователя
 * @param {Object} credentials - { username, password }
 */
export async function registerUser({ username, password }) {
  console.log(`[API] Отправка запроса на регистрацию пользователя: ${username}`);
  const response = await fetch(`${API_BASE}/api/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  });
  return handleResponse(response);
}

/**
 * Аутентификация пользователя (вход)
 * @param {Object} credentials - { username, password }
 */
export async function loginUser({ username, password }) {
  console.log(`[API] Отправка запроса на вход пользователя: ${username}`);
  const response = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  });
  return handleResponse(response);
}

/**
 * Отправка сообщения в защищенный эндпоинт AI-чата.
 * Автоматически использует apiClient с добавлением токена в заголовок Authorization.
 *
 * @param {string} message - Текст сообщения пользователя
 * @param {string} [token] - Опциональный токен авторизации (если опущен, автоматически берется из localStorage)
 * @returns {Promise<{response: string, timestamp: string}>}
 */
export async function sendChatMessage(message, token) {
  console.log(`[API] Отправка сообщения в AI-чат (длина: ${message?.length || 0} символов)`);
  const options = token !== undefined ? { token } : {};
  return apiClient.post('/api/chat', { message }, options);
}

/**
 * Проверка доступности Spring Boot бэкенда
 */
export async function checkBackendHealth() {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000);
    const response = await fetch(`${API_BASE}/hello`, {
      method: 'GET',
      signal: controller.signal,
    });
    clearTimeout(timeoutId);
    if (response.ok) {
      const text = await response.text();
      return { online: true, message: text };
    }
    return { online: false, message: `HTTP ${response.status}` };
  } catch (err) {
    return { online: false, message: err.message };
  }
}
