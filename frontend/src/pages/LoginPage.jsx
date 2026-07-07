import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Toast from '../components/Toast.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { loginUser } from '../api/client.js';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated } = useAuth();
  const redirectPath = useRef(location.state?.from?.pathname || '/chat');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [notification, setNotification] = useState(location.state?.notification ?? '');

  useEffect(() => {
    if (isAuthenticated) {
      navigate(redirectPath.current, { replace: true });
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    if (location.state?.notification) {
      setNotification(location.state.notification);
      navigate(location.pathname, {
        replace: true,
        state: { from: location.state.from },
      });
    }
  }, [location.pathname, location.state, navigate]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const data = await loginUser({ username, password });
      login(data.token, data.username);
      navigate(redirectPath.current, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <Toast message={notification} onClose={() => setNotification('')} />
      <div className="card">
        <h1>Login</h1>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </div>
          {error && <p className="error-text">{error}</p>}
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className="link-row">
          No account? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  );
}
