import React from 'react';

export const LoadingSpinner: React.FC<{ message?: string }> = ({
  message = 'Loading data...',
}) => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '48px 24px',
        color: 'var(--text-secondary)',
        gap: '12px',
      }}
    >
      <div
        style={{
          width: '28px',
          height: '28px',
          border: '2px solid rgba(6, 182, 212, 0.2)',
          borderTopColor: 'var(--cyan-400)',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite',
        }}
      />
      <span style={{ fontSize: '13px', fontFamily: 'var(--font-mono)' }}>
        {message}
      </span>
      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};
