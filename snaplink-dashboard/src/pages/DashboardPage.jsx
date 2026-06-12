import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client.js';
import toast from 'react-hot-toast';
import { Link2, Plus, BarChart3, Trash2, Copy, QrCode, ExternalLink, MousePointerClick } from 'lucide-react';
import QrCodeModal from '../components/QrCodeModal.jsx';

export default function DashboardPage() {
  const [links, setLinks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [qrModal, setQrModal] = useState(null);

  useEffect(() => {
    fetchLinks();
  }, []);

  const fetchLinks = async () => {
    try {
      const data = await api.listLinks();
      setLinks(data);
    } catch (err) {
      toast.error('Failed to load links');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (code) => {
    if (!window.confirm(`Delete link /${code}? This cannot be undone.`)) return;
    try {
      await api.deleteLink(code);
      setLinks(links.filter(l => l.shortCode !== code));
      toast.success('Link deleted');
    } catch (err) {
      toast.error(err.message);
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    toast.success('Copied to clipboard!');
  };

  const getStatus = (expiresAt) => {
    if (!expiresAt) return 'never';
    return new Date(expiresAt) > new Date() ? 'active' : 'expired';
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    });
  };

  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

  if (loading) {
    return <div className="loading-page"><div className="spinner" style={{ width: 40, height: 40 }} /></div>;
  }

  return (
    <div className="animate-in">
      {/* Header */}
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Manage your short links and track performance</p>
        </div>
        <Link to="/create" className="btn btn-primary">
          <Plus size={18} />
          Create New Link
        </Link>
      </div>

      {/* Stats Row */}
      <div className="grid-stats">
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(99, 102, 241, 0.1)' }}>
            <Link2 size={20} color="var(--accent-indigo)" />
          </div>
          <div className="stat-label">Total Links</div>
          <div className="stat-value">{links.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(16, 185, 129, 0.1)' }}>
            <MousePointerClick size={20} color="var(--accent-emerald)" />
          </div>
          <div className="stat-label">Total Clicks</div>
          <div className="stat-value">{links.reduce((sum, l) => sum + (l.clickCount || 0), 0)}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(6, 182, 212, 0.1)' }}>
            <BarChart3 size={20} color="var(--accent-cyan)" />
          </div>
          <div className="stat-label">Active Links</div>
          <div className="stat-value">{links.filter(l => getStatus(l.expiresAt) !== 'expired').length}</div>
        </div>
      </div>

      {/* Links Table */}
      {links.length === 0 ? (
        <div className="empty-state card">
          <Link2 size={48} style={{ opacity: 0.2, marginBottom: 16 }} />
          <h3 style={{ marginBottom: 8 }}>No links yet</h3>
          <p style={{ marginBottom: 20 }}>Create your first short link to get started</p>
          <Link to="/create" className="btn btn-primary">
            <Plus size={18} /> Create Link
          </Link>
        </div>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Short Link</th>
                <th>Destination</th>
                <th>Created</th>
                <th>Status</th>
                <th>Clicks</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {links.map((link, idx) => {
                const status = getStatus(link.expiresAt);
                const shortUrl = `${baseUrl}/${link.shortCode}`;
                return (
                  <tr key={link.shortCode} style={{ animation: `slideInRight ${0.15 + idx * 0.05}s ease-out` }}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <code style={{
                          background: 'var(--bg-glass)',
                          padding: '4px 10px',
                          borderRadius: 'var(--radius-sm)',
                          fontSize: '0.85rem',
                          color: 'var(--accent-indigo-light)',
                          fontWeight: 600,
                        }}>
                          /{link.shortCode}
                        </code>
                        <button onClick={() => copyToClipboard(shortUrl)} className="btn btn-icon btn-secondary" title="Copy link">
                          <Copy size={14} />
                        </button>
                      </div>
                    </td>
                    <td>
                      <div style={{
                        maxWidth: 250,
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                        color: 'var(--text-secondary)',
                        fontSize: '0.85rem',
                      }}>
                        <a href={link.longUrl} target="_blank" rel="noopener noreferrer"
                           style={{ color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: 4 }}>
                          {link.longUrl}
                          <ExternalLink size={12} />
                        </a>
                      </div>
                    </td>
                    <td style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                      {formatDate(link.createdAt)}
                    </td>
                    <td>
                      <span className={`badge badge-${status}`}>
                        {status === 'never' ? '∞ Active' : status === 'active' ? 'Active' : 'Expired'}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>{link.clickCount || 0}</span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                        <Link to={`/analytics/${link.shortCode}`} className="btn btn-secondary btn-sm" title="Analytics">
                          <BarChart3 size={14} />
                        </Link>
                        <button onClick={() => setQrModal(link)} className="btn btn-secondary btn-sm" title="QR Code">
                          <QrCode size={14} />
                        </button>
                        <button onClick={() => handleDelete(link.shortCode)} className="btn btn-danger btn-sm" title="Delete">
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* QR Code Modal */}
      {qrModal && (
        <QrCodeModal
          shortCode={qrModal.shortCode}
          qrCodeUrl={qrModal.qrCodeUrl}
          onClose={() => setQrModal(null)}
        />
      )}
    </div>
  );
}
