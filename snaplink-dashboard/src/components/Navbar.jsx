import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { Link2, LogOut, Plus, BarChart3 } from 'lucide-react';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav style={{
      borderBottom: '1px solid var(--border-color)',
      backdropFilter: 'blur(20px)',
      background: 'rgba(10, 14, 26, 0.8)',
      position: 'sticky',
      top: 0,
      zIndex: 100,
    }}>
      <div className="container" style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: 64,
      }}>
        {/* Logo */}
        <Link to="/" style={{
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          textDecoration: 'none',
        }}>
          <div style={{
            width: 36,
            height: 36,
            background: 'var(--gradient-primary)',
            borderRadius: 'var(--radius-md)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}>
            <Link2 size={20} color="white" />
          </div>
          <span style={{
            fontFamily: 'var(--font-heading)',
            fontSize: '1.25rem',
            fontWeight: 800,
            background: 'var(--gradient-primary)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
          }}>
            SnapLink
          </span>
        </Link>

        {/* Nav Actions */}
        {isAuthenticated ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <Link to="/create" className="btn btn-primary btn-sm">
              <Plus size={16} />
              New Link
            </Link>
            <Link to="/" className="btn btn-secondary btn-sm">
              <BarChart3 size={16} />
              Dashboard
            </Link>
            <div style={{
              width: 1,
              height: 24,
              background: 'var(--border-color)',
              margin: '0 4px',
            }} />
            <span style={{
              fontSize: '0.8rem',
              color: 'var(--text-muted)',
              maxWidth: 150,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}>
              {user?.email}
            </span>
            <button onClick={handleLogout} className="btn btn-secondary btn-icon" title="Log out">
              <LogOut size={16} />
            </button>
          </div>
        ) : (
          <div style={{ display: 'flex', gap: 8 }}>
            <Link to="/login" className="btn btn-secondary btn-sm">Log In</Link>
            <Link to="/register" className="btn btn-primary btn-sm">Sign Up</Link>
          </div>
        )}
      </div>
    </nav>
  );
}
