import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const PROTECTED_REDIRECT_MESSAGE = 'You must be logged in to access the chat.';

export default function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        replace
        state={{
          from: location,
          notification: PROTECTED_REDIRECT_MESSAGE,
        }}
      />
    );
  }

  return children;
}
