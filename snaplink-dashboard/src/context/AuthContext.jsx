import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check for existing token on mount
    const token = localStorage.getItem('snaplink_token');
    const email = localStorage.getItem('snaplink_email');
    if (token && email) {
      setUser({ email, token });
    }
    setLoading(false);
  }, []);

  const login = (email, token) => {
    localStorage.setItem('snaplink_token', token);
    localStorage.setItem('snaplink_email', email);
    setUser({ email, token });
  };

  const logout = () => {
    localStorage.removeItem('snaplink_token');
    localStorage.removeItem('snaplink_email');
    setUser(null);
  };

  const isAuthenticated = !!user;

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
