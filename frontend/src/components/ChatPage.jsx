import React from 'react';
import Chat from './Chat';

/**
 * Страница AI-чата (ChatPage).
 * Оборачивает переиспользуемый компонент {@link Chat}.
 */
export default function ChatPage(props) {
  return <Chat {...props} />;
}
