/**
 * API клиент для взаимодействия со Spring Boot бэкендом
 */

const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

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
      errorMessage = data.error || data.message || `Ошибка сервера (${response.status})`;
      errorDetails = data.details || null;
    } else if (typeof data === 'string' && data.length > 0) {
      errorMessage = data;
    }

    const error = new Error(errorMessage);
    error.status = response.status;
    error.details = errorDetails;
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
