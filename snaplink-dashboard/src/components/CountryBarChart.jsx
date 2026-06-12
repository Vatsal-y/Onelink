import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
} from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip);

const COUNTRY_FLAGS = {
  US: '🇺🇸', GB: '🇬🇧', DE: '🇩🇪', FR: '🇫🇷', IN: '🇮🇳',
  CA: '🇨🇦', AU: '🇦🇺', JP: '🇯🇵', BR: '🇧🇷', CN: '🇨🇳',
  KR: '🇰🇷', MX: '🇲🇽', ES: '🇪🇸', IT: '🇮🇹', NL: '🇳🇱',
  XX: '🌍',
};

export default function CountryBarChart({ data }) {
  const entries = Object.entries(data || {})
    .sort(([, a], [, b]) => b - a)
    .slice(0, 5);

  if (entries.length === 0) {
    return <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No country data yet</div>;
  }

  const chartData = {
    labels: entries.map(([code]) => `${COUNTRY_FLAGS[code] || '🌍'} ${code}`),
    datasets: [
      {
        data: entries.map(([, val]) => val),
        backgroundColor: [
          'rgba(99, 102, 241, 0.7)',
          'rgba(6, 182, 212, 0.7)',
          'rgba(16, 185, 129, 0.7)',
          'rgba(245, 158, 11, 0.7)',
          'rgba(244, 63, 94, 0.7)',
        ],
        borderColor: [
          '#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#f43f5e',
        ],
        borderWidth: 1,
        borderRadius: 6,
        maxBarThickness: 40,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1e293b',
        titleColor: '#f1f5f9',
        bodyColor: '#94a3b8',
        borderColor: 'rgba(255,255,255,0.08)',
        borderWidth: 1,
        padding: 12,
        cornerRadius: 8,
        callbacks: {
          label: (ctx) => `${ctx.parsed.x} clicks`,
        },
      },
    },
    scales: {
      x: {
        beginAtZero: true,
        grid: { color: 'rgba(255,255,255,0.04)', drawBorder: false },
        ticks: {
          color: '#64748b',
          font: { size: 11 },
          stepSize: 1,
          callback: (val) => Number.isInteger(val) ? val : '',
        },
      },
      y: {
        grid: { display: false },
        ticks: { color: '#f1f5f9', font: { size: 13 } },
      },
    },
  };

  return (
    <div style={{ height: 250 }}>
      <Bar data={chartData} options={options} />
    </div>
  );
}
