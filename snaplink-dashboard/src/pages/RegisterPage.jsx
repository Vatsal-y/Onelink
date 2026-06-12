import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/client.js';
import toast from 'react-hot-toast';
import { UserPlus, Mail, Lock } from 'lucide-react';

export default function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }
    setLoading(true);
    try {
      await api.register(email, password);
      toast.success('Account created! Please log in.');
      navigate('/login');
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
          Create Account
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>
          Start shortening URLs in seconds
        </p>
      </div>

      <div className="card" style={{ padding: 32 }}>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div className="form-group">
            <label className="form-label" htmlFor="reg-email">
              <Mail size={14} style={{ display: 'inline', marginRight: 6, verticalAlign: '-2px' }} />
              Email
            </label>
            <input
              id="reg-email"
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
            <label className="form-label" htmlFor="reg-password">
              <Lock size={14} style={{ display: 'inline', marginRight: 6, verticalAlign: '-2px' }} />
              Password
            </label>
            <input
              id="reg-password"
              className="form-input"
              type="password"
              placeholder="Min. 8 characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="reg-confirm">
              <Lock size={14} style={{ display: 'inline', marginRight: 6, verticalAlign: '-2px' }} />
              Confirm Password
            </label>
            <input
              id="reg-confirm"
              className="form-input"
              type="password"
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
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
            {loading ? <div className="spinner" style={{ width: 18, height: 18 }} /> : <><UserPlus size={18} /> Create Account</>}
          </button>
        </form>
      </div>

      <p style={{
        textAlign: 'center',
        marginTop: 20,
        color: 'var(--text-secondary)',
        fontSize: '0.875rem',
      }}>
        Already have an account?{' '}
        <Link to="/login" style={{ fontWeight: 600 }}>Sign in</Link>
      </p>
    </div>
  );
}
