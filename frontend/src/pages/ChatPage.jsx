import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { sendChatMessage } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function ChatPage() {
  const navigate = useNavigate();
  const { token, username, logout } = useAuth();
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const trimmed = message.trim();
    if (!trimmed) {
      return;
    }

    setError('');
    setLoading(true);
    setMessages((current) => [...current, { role: 'user', content: trimmed }]);
    setMessage('');

    try {
      const data = await sendChatMessage(trimmed, token);
      setMessages((current) => [...current, { role: 'assistant', content: data.reply }]);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <div className="nav-bar">
          <h1>Chat</h1>
          <div>
            <span style={{ marginRight: '1rem' }}>Hi, {username}</span>
            <button className="btn btn-secondary" type="button" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </div>

        <div className="chat-messages">
          {messages.length === 0 && <p>Ask the assistant anything to get started.</p>}
          {messages.map((entry, index) => (
            <div key={`${entry.role}-${index}`} className={`chat-bubble ${entry.role}`}>
              {entry.content}
            </div>
          ))}
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="message">Message</label>
            <textarea
              id="message"
              rows={3}
              value={message}
              onChange={(event) => setMessage(event.target.value)}
              placeholder="Type your message..."
              required
            />
          </div>
          {error && <p className="error-text">{error}</p>}
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? 'Sending...' : 'Send'}
          </button>
        </form>
      </div>
    </div>
  );
}
