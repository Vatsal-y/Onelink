import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client.js';
import toast from 'react-hot-toast';
import { Link2, Sparkles, Clock, Copy, QrCode, Check } from 'lucide-react';

const EXPIRY_OPTIONS = [
  { value: 'never', label: 'Never expires', icon: '∞' },
  { value: '1h', label: '1 Hour', icon: '⏱' },
  { value: '1d', label: '1 Day', icon: '📅' },
  { value: '7d', label: '7 Days', icon: '📆' },
  { value: '30d', label: '30 Days', icon: '🗓' },
];

export default function CreateLinkPage() {
  const [longUrl, setLongUrl] = useState('');
  const [alias, setAlias] = useState('');
  const [expiresIn, setExpiresIn] = useState('never');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [copied, setCopied] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setResult(null);
    try {
      const data = await api.shortenUrl(longUrl, alias, expiresIn);
      setResult(data);
      toast.success('Short link created!');
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  const copyLink = () => {
    navigator.clipboard.writeText(result.shortUrl);
    setCopied(true);
    toast.success('Copied!');
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="animate-in" style={{ maxWidth: 640, margin: '0 auto' }}>
      <div className="page-header">
        <h1 className="page-title">Create Short Link</h1>
        <p className="page-subtitle">Shorten a URL, set a custom alias, and choose an expiry</p>
      </div>

      <div className="card" style={{ padding: 32 }}>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          {/* Long URL */}
          <div className="form-group">
            <label className="form-label" htmlFor="create-url">
              <Link2 size={14} style={{ display: 'inline', marginRight: 6, verticalAlign: '-2px' }} />
              Destination URL
            </label>
            <input
              id="create-url"
              className="form-input"
              type="url"
              placeholder="https://www.example.com/very/long/path/to/page"
              value={longUrl}
              onChange={(e) => setLongUrl(e.target.value)}
              required
              autoFocus
              style={{ fontSize: '1rem' }}
            />
          </div>

          {/* Custom Alias */}
          <div className="form-group">
            <label className="form-label" htmlFor="create-alias">
              <Sparkles size={14} style={{ display: 'inline', marginRight: 6, verticalAlign: '-2px' }} />
              Custom Alias <span style={{ color: 'var(--text-muted)', fontWeight: 400 }}>(optional)</span>
            </label>
            <div style={{ position: 'relative' }}>
              <span style={{
                position: 'absolute',
                left: 16,
                top: '50%',
                transform: 'translateY(-50%)',
                color: 'var(--text-muted)',
                fontSize: '0.9rem',
                userSelect: 'none',
              }}>
                snpl.ink/
              </span>
              <input
                id="create-alias"
                className="form-input"
                type="text"
                placeholder="my-launch"
                value={alias}
                onChange={(e) => setAlias(e.target.value)}
                minLength={3}
                maxLength={30}
                pattern="^[a-zA-Z0-9-]*$"
                style={{ paddingLeft: 82, fontSize: '1rem' }}
              />
            </div>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              3–30 characters. Letters, numbers, and hyphens only.
            </span>
          </div>

          {/* Expiry */}
          <div className="form-group">
            <label className="form-label">
              <Clock size={14} style={{ display: 'inline', marginRight: 6, verticalAlign: '-2px' }} />
              Link Expiry
            </label>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {EXPIRY_OPTIONS.map(opt => (
                <button
                  key={opt.value}
                  type="button"
                  onClick={() => setExpiresIn(opt.value)}
                  className={expiresIn === opt.value ? 'btn btn-primary btn-sm' : 'btn btn-secondary btn-sm'}
                  style={{ minWidth: 90 }}
                >
                  {opt.icon} {opt.label}
                </button>
              ))}
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
            style={{ width: '100%', padding: '14px 24px', fontSize: '1rem' }}
          >
            {loading ? (
              <div className="spinner" style={{ width: 20, height: 20 }} />
            ) : (
              <><Sparkles size={20} /> Shorten URL</>
            )}
          </button>
        </form>
      </div>

      {/* Result Card */}
      {result && (
        <div className="card" style={{
          marginTop: 24,
          padding: 32,
          background: 'var(--gradient-card)',
          animation: 'slideUp 0.3s ease-out',
        }}>
          <h3 style={{ marginBottom: 16, color: 'var(--accent-emerald)', display: 'flex', alignItems: 'center', gap: 8 }}>
            <Check size={20} /> Link Created Successfully
          </h3>

          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            background: 'var(--bg-input)',
            borderRadius: 'var(--radius-md)',
            padding: '12px 16px',
            marginBottom: 16,
          }}>
            <code style={{
              flex: 1,
              fontSize: '1.1rem',
              fontWeight: 600,
              color: 'var(--accent-indigo-light)',
              wordBreak: 'break-all',
            }}>
              {result.shortUrl}
            </code>
            <button onClick={copyLink} className="btn btn-primary btn-sm">
              {copied ? <><Check size={16} /> Copied!</> : <><Copy size={16} /> Copy</>}
            </button>
          </div>

          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
            {result.qrCodeUrl && (
              <a href={result.qrCodeUrl} target="_blank" rel="noopener noreferrer" className="btn btn-secondary btn-sm">
                <QrCode size={16} /> Download QR Code
              </a>
            )}
            <button onClick={() => navigate('/')} className="btn btn-secondary btn-sm">
              View Dashboard
            </button>
            <button onClick={() => { setResult(null); setLongUrl(''); setAlias(''); }} className="btn btn-secondary btn-sm">
              Create Another
            </button>
          </div>

          {result.expiresAt && (
            <p style={{ marginTop: 12, fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Expires: {new Date(result.expiresAt).toLocaleString()}
            </p>
          )}
        </div>
      )}
    </div>
  );
}
