import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import api from '../api/client.js';
import toast from 'react-hot-toast';
import { LogIn, Mail, Lock } from 'lucide-react';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  if (isAuthenticated) {
    navigate('/', { replace: true });
    return null;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const data = await api.login(email, password);
      login(email, data.accessToken || data.idToken);
      toast.success('Welcome back!');
      navigate('/');
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      maxWidth: 440,
      margin: '60px auto',
      animation: 'slideUp 0.4s ease-out',
    }}>
      <div style={{ textAlign: 'center', marginBottom: 32 }}>
        <h1 style={{
          fontSize: '2rem',
          fontWeight: 800,
          marginBottom: 8,
          background: 'var(--gradient-primary)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          backgroundClip: 'text',
        }}>
          Welcome Back
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>
          Sign in to manage your short links
        </p>
      </div>

      <div className="card" style={{ padding: 32 }}>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div className="form-group">
            <label className="form-label" htmlFor="login-email">
              <Mail size={14} style={{ display: 'inline', marginRight: 6, verticalAlign: '-2px' }} />
              Email
            </label>
            <input
              id="login-email"
              className="form-input"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoFocus
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="login-password">
              <Lock size={14} style={{ display: 'inline', marginRight: 6, verticalAlign: '-2px' }} />
              Password
            </label>
            <input
              id="login-password"
              className="form-input"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
            style={{ width: '100%', padding: '12px 24px', marginTop: 8 }}
          >
            {loading ? <div className="spinner" style={{ width: 18, height: 18 }} /> : <><LogIn size={18} /> Sign In</>}
          </button>
        </form>
      </div>

      <p style={{
        textAlign: 'center',
        marginTop: 20,
        color: 'var(--text-secondary)',
        fontSize: '0.875rem',
      }}>
        Don&apos;t have an account?{' '}
        <Link to="/register" style={{ fontWeight: 600 }}>Sign up</Link>
      </p>
    </div>
  );
}
