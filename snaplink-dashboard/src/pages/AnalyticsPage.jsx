import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/client.js';
import toast from 'react-hot-toast';
import { ArrowLeft, MousePointerClick, Users, Globe, Smartphone } from 'lucide-react';
import ClicksChart from '../components/ClicksChart.jsx';
import DevicePieChart from '../components/DevicePieChart.jsx';
import CountryBarChart from '../components/CountryBarChart.jsx';

export default function AnalyticsPage() {
  const { code } = useParams();
  const [analytics, setAnalytics] = useState(null);
  const [days, setDays] = useState(7);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalytics();
  }, [code, days]);

  const fetchAnalytics = async () => {
    setLoading(true);
    try {
      const data = await api.getAnalytics(code, days);
      setAnalytics(data);
    } catch (err) {
      toast.error('Failed to load analytics');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading-page"><div className="spinner" style={{ width: 40, height: 40 }} /></div>;
  }

  if (!analytics) {
    return (
      <div className="empty-state">
        <h3>No analytics data found</h3>
        <p>This link may not exist or has no click data yet.</p>
        <Link to="/" className="btn btn-primary" style={{ marginTop: 16 }}>Back to Dashboard</Link>
      </div>
    );
  }

  return (
    <div className="animate-in">
      {/* Header */}
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16 }}>
        <div>
          <Link to="/" style={{
            display: 'inline-flex', alignItems: 'center', gap: 6,
            fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: 8,
          }}>
            <ArrowLeft size={14} /> Back to Dashboard
          </Link>
          <h1 className="page-title" style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            Analytics
            <code style={{
              background: 'var(--bg-glass)',
              padding: '4px 14px',
              borderRadius: 'var(--radius-md)',
              fontSize: '1.2rem',
              color: 'var(--accent-indigo-light)',
            }}>
              /{code}
            </code>
          </h1>
        </div>

        {/* Date range selector */}
        <div style={{ display: 'flex', gap: 6 }}>
          {[7, 14, 30].map(d => (
            <button
              key={d}
              onClick={() => setDays(d)}
              className={days === d ? 'btn btn-primary btn-sm' : 'btn btn-secondary btn-sm'}
            >
              {d}d
            </button>
          ))}
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid-stats">
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(99, 102, 241, 0.1)' }}>
            <MousePointerClick size={20} color="var(--accent-indigo)" />
          </div>
          <div className="stat-label">Total Clicks</div>
          <div className="stat-value">{analytics.totalClicks.toLocaleString()}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(16, 185, 129, 0.1)' }}>
            <Users size={20} color="var(--accent-emerald)" />
          </div>
          <div className="stat-label">Unique Clicks</div>
          <div className="stat-value">{analytics.uniqueClicks.toLocaleString()}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(6, 182, 212, 0.1)' }}>
            <Globe size={20} color="var(--accent-cyan)" />
          </div>
          <div className="stat-label">Countries</div>
          <div className="stat-value">
            {analytics.clicksByCountry ? Object.keys(analytics.clicksByCountry).length : 0}
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(245, 158, 11, 0.1)' }}>
            <Smartphone size={20} color="var(--accent-amber)" />
          </div>
          <div className="stat-label">Mobile %</div>
          <div className="stat-value">
            {analytics.totalClicks > 0 && analytics.clicksByDevice?.mobile
              ? Math.round((analytics.clicksByDevice.mobile / analytics.totalClicks) * 100)
              : 0}%
          </div>
        </div>
      </div>

      {/* Charts */}
      <div style={{ marginBottom: 24 }}>
        <div className="card">
          <h3 style={{ marginBottom: 16 }}>Clicks Over Time</h3>
          <ClicksChart data={analytics.clicksOverTime || []} />
        </div>
      </div>

      <div className="grid-2">
        <div className="card">
          <h3 style={{ marginBottom: 16 }}>Device Breakdown</h3>
          <DevicePieChart data={analytics.clicksByDevice || {}} />
        </div>
        <div className="card">
          <h3 style={{ marginBottom: 16 }}>Top Countries</h3>
          <CountryBarChart data={analytics.clicksByCountry || {}} />
        </div>
      </div>
    </div>
  );
}
