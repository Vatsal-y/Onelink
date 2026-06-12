import { Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';

ChartJS.register(ArcElement, Tooltip, Legend);

const DEVICE_COLORS = {
  desktop: '#6366f1',
  mobile: '#06b6d4',
  bot: '#64748b',
};

const DEVICE_LABELS = {
  desktop: '💻 Desktop',
  mobile: '📱 Mobile',
  bot: '🤖 Bot',
};

export default function DevicePieChart({ data }) {
  const entries = Object.entries(data || {});

  if (entries.length === 0) {
    return <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No device data yet</div>;
  }

  const chartData = {
    labels: entries.map(([key]) => DEVICE_LABELS[key] || key),
    datasets: [
      {
        data: entries.map(([, val]) => val),
        backgroundColor: entries.map(([key]) => DEVICE_COLORS[key] || '#94a3b8'),
        borderColor: '#0a0e1a',
        borderWidth: 3,
        hoverOffset: 6,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '65%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          color: '#94a3b8',
          padding: 16,
          font: { size: 12 },
          usePointStyle: true,
          pointStyleWidth: 10,
        },
      },
      tooltip: {
        backgroundColor: '#1e293b',
        titleColor: '#f1f5f9',
        bodyColor: '#94a3b8',
        borderColor: 'rgba(255,255,255,0.08)',
        borderWidth: 1,
        padding: 12,
        cornerRadius: 8,
      },
    },
  };

  return (
    <div style={{ height: 250 }}>
      <Doughnut data={chartData} options={options} />
    </div>
  );
}
