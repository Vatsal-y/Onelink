import { X, Download, ExternalLink } from 'lucide-react';

export default function QrCodeModal({ shortCode, qrCodeUrl, onClose }) {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>QR Code</h3>
          <button onClick={onClose} className="btn btn-icon btn-secondary">
            <X size={18} />
          </button>
        </div>

        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 20,
        }}>
          {/* QR Code Preview */}
          <div style={{
            width: 220,
            height: 220,
            background: 'white',
            borderRadius: 'var(--radius-lg)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: 16,
          }}>
            {qrCodeUrl ? (
              <img
                src={qrCodeUrl}
                alt={`QR code for /${shortCode}`}
                style={{ width: '100%', height: '100%', objectFit: 'contain' }}
              />
            ) : (
              <div style={{
                width: '100%',
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#94a3b8',
                fontSize: '0.85rem',
                textAlign: 'center',
              }}>
                QR code preview<br />unavailable
              </div>
            )}
          </div>

          {/* Short Code Badge */}
          <code style={{
            background: 'var(--bg-glass)',
            padding: '6px 16px',
            borderRadius: 'var(--radius-full)',
            fontSize: '0.9rem',
            color: 'var(--accent-indigo-light)',
            fontWeight: 600,
          }}>
            /{shortCode}
          </code>

          {/* Actions */}
          <div style={{ display: 'flex', gap: 10 }}>
            {qrCodeUrl && (
              <a
                href={qrCodeUrl}
                target="_blank"
                rel="noopener noreferrer"
                download={`qr-${shortCode}.png`}
                className="btn btn-primary"
              >
                <Download size={16} /> Download PNG
              </a>
            )}
            <button onClick={onClose} className="btn btn-secondary">
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
