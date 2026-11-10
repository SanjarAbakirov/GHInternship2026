import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach, vi } from 'vitest';

// Автоматическая очистка DOM после каждого теста
afterEach(() => {
  cleanup();
});

// Полифилл scrollIntoView для jsdom (отсутствует по умолчанию в jsdom)
if (typeof window !== 'undefined' && !window.HTMLElement.prototype.scrollIntoView) {
  window.HTMLElement.prototype.scrollIntoView = vi.fn();
}
