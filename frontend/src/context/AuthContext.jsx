import { createContext, useCallback, useContext, useMemo, useState } from 'react';

const TOKEN_KEY = 'authToken';
const USERNAME_KEY = 'username';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [username, setUsername] = useState(() => localStorage.getItem(USERNAME_KEY));

  const login = useCallback((authToken, authUsername) => {
    localStorage.setItem(TOKEN_KEY, authToken);
    localStorage.setItem(USERNAME_KEY, authUsername);
    setToken(authToken);
    setUsername(authUsername);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    setToken(null);
    setUsername(null);
  }, []);

  const value = useMemo(
    () => ({
      token,
      username,
      isAuthenticated: Boolean(token),
      login,
      logout,
    }),
    [token, username, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
